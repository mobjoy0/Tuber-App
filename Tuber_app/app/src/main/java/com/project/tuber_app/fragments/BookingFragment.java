package com.project.tuber_app.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.project.tuber_app.R;
import com.project.tuber_app.activites.CreateRideActivity;
import com.project.tuber_app.adapters.BookingRequestAdapter;
import com.project.tuber_app.adapters.ConfirmedRequestAdapter;
import com.project.tuber_app.adapters.PendingRequestAdapter;
import com.project.tuber_app.api.ApiClient;
import com.project.tuber_app.api.BookingApi;
import com.project.tuber_app.api.RideApi;
import com.project.tuber_app.databases.Database;
import com.project.tuber_app.databases.UserEntity;
import com.project.tuber_app.entities.Booking;
import com.project.tuber_app.entities.Ride;
import com.project.tuber_app.entities.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingFragment extends Fragment {

    private TextInputEditText fromInput;
    private TextInputEditText toInput;
    private MaterialButton searchButton;
    private static final String TAG = "BookingFragment";

    private User user;
    private Ride ride;
    private Booking booking;
    private List<Booking> pendingBooking, confirmedBooking;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private RideApi rideApi;
    private BookingApi bookingApi;
    private RecyclerView pendingRecyclerView, confirmedRecyclerView;
    private PendingRequestAdapter pendingAdapter;

    private ConfirmedRequestAdapter confirmedAdapter;
    private Button cancelRideDriver;
    private Database db;

    public BookingFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = Database.getInstance(getContext());
        rideApi = ApiClient.getRideApi(getContext());
        bookingApi = ApiClient.getBookingApi(getContext());

        executorService.submit(() -> {
            UserEntity userEntity = db.userDao().getUser();

            requireActivity().runOnUiThread(() -> {
                try {
                    if (userEntity.role == UserEntity.Role.DRIVER) {
                        Toast.makeText(requireContext(), "Fetching ride data as driver...", Toast.LENGTH_SHORT).show();
                        retrieveRideIfExists(view);
                    } else {
                        Toast.makeText(getContext(), "Passenger view loading...", Toast.LENGTH_SHORT).show();
                        retrieveConfirmedBookingIfExists(view);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading user role: " + e.getMessage());
                }
            });
        });

        cancelRideDriver = view.findViewById(R.id.cancelRideDriver);

        cancelRideDriver.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cancel Ride")
                    .setMessage("Are you sure you want to cancel this ride?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        cancelRideDriverApi();
                        Toast.makeText(requireContext(), "Ride cancelled.", Toast.LENGTH_SHORT).show();

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .detach(this)
                                .attach(this)
                                .commit();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });


    }

    public void retrieveRideIfExists(View view) {
        rideApi.getRideByDriver().enqueue(new Callback<Ride>() {
            @Override
            public void onResponse(@NonNull Call<Ride> call, @NonNull Response<Ride> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ride = response.body();
                    Log.d(TAG, "Scheduled ride found: " + ride);
                    view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    view.findViewById(R.id.rideHubCard).setVisibility(View.VISIBLE);
                    loadRide(ride.getId(), view);
                } else {
                    //TODO: ui for driver no he has no ride
                    retrieveConfirmedBookingIfExists(view);
                    Log.d(TAG, "No scheduled ride found here");
                    Toast.makeText(getContext(), "No scheduled ride found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Ride> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(getContext(), "Failed to retrieve ride", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadRide(int rideId, View view) {
        fetchConfirmedBooking(view, rideId);
        fetchPendingBookings(view, rideId);
        countdownTick(view);
    }

    private void fetchConfirmedBooking(View view, int rideId) {
        bookingApi.getConfirmedBooking(rideId).enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<List<Booking>> call, @NonNull Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    confirmedRecyclerView = view.findViewById(R.id.confirmedPassagersRecycle);

                    confirmedBooking = response.body();
                    Log.wtf("ee", "confiremd passagers = " + confirmedBooking.size());
                    confirmedAdapter = new ConfirmedRequestAdapter(confirmedBooking);
                    confirmedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    confirmedRecyclerView.setAdapter(confirmedAdapter);
                } else {
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to fetch confrimed bookings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPendingBookings(View view, int rideId) {
        bookingApi.getPendingBooking(rideId).enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<List<Booking>> call, @NonNull Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    pendingRecyclerView = view.findViewById(R.id.pendingRequestsRecycle);
                    view.findViewById(R.id.progressBar).setVisibility(View.GONE);

                    pendingBooking = response.body();
                    Log.wtf("ee", "pending passagers = " + pendingBooking.size());
                    pendingAdapter = new PendingRequestAdapter(pendingBooking, new PendingRequestAdapter.OnBookingActionClick() {
                        @Override
                        public void onAccept(Booking booking) {
                            changeRequestStatus(booking.getId() ,"CONFIRMED");
                        }

                        @Override
                        public void onReject(Booking booking) {
                            changeRequestStatus(booking.getId() ,"CANCELED");
                            pendingAdapter.notifyDataSetChanged();


                        }
                    });
                    pendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    pendingRecyclerView.setAdapter(pendingAdapter);
                } else {
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
            }
        });
    }



    private void retrieveConfirmedBookingIfExists(View view){
        bookingApi.getBooking("CONFIRMED").enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<List<Booking>> call, @NonNull Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    List<Booking> bookings = response.body();
                    Log.d(TAG, "confirmed booking found: " + bookings);
                    view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    LoadRideDetailsSectionUI(bookings.get(0), view);
                } else {

                    //TODO: ui for when user has no confirmed booking *fuction* so it can be reused for when the driver has no ride
                    Log.d(TAG, " non confirmed booking found");
                    loadPendingRequestsIfExist(view);

                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed", t);
            }
        });

    }

    private void LoadRideDetailsSectionUI(Booking booking, View view) {
        // Get the root layout for the section
        try {
            Log.wtf("ee", "bookong is:" + booking.getRide());
            CardView rideDetailsSection = view.findViewById(R.id.rideDetailsCard);
            rideDetailsSection.setVisibility(View.VISIBLE); // Make it visible if hidden

            // Departure Section
            TextView departureText = view.findViewById(R.id.departureText);
            TextView departureTime = view.findViewById(R.id.departureTime);
            departureText.setText(booking.getRide().getStartLocation()); // Assuming `getDepartureLocation()` method
            departureTime.setText(booking.getRide().getRideTime()); // Assuming `getDepartureTime()` is LocalDateTime

            // Arrival Section
            TextView arrivalText = view.findViewById(R.id.arrivalText);
            TextView arrivalTime = view.findViewById(R.id.arrivalTime);
            arrivalText.setText(booking.getRide().getEndLocation()); // Assuming `getArrivalLocation()`
            arrivalTime.setText(booking.getRide().getRideEndTime()); // Assuming `getArrivalTime()` is LocalDateTime

            // Distance Section
            TextView distanceValue = view.findViewById(R.id.distanceValue);
            distanceValue.setText(booking.getRide().getDistance() + " km"); // Assuming `getDistance()` returns distance

            // Duration Section
            TextView durationValue = view.findViewById(R.id.durationValue);
            durationValue.setText(booking.getRide().getRideDuration()); // Assuming `getDuration()` returns duration as string (e.g. "1h 32m")

            // Booking Summary Section
            TextView seatSummaryText = view.findViewById(R.id.seatSummaryText);
            seatSummaryText.setText("You have booked " + booking.getSeatsBooked() + " seats for " + booking.getRide().getPrice() * booking.getSeatsBooked() + "dt."); // Assuming `getPrice()` returns price

            ride = booking.getRide();
            countdownTick(view);

            // Cancel Button Section
            Button cancelButton = view.findViewById(R.id.cancelButton);
            cancelButton.setOnClickListener(v -> {
                changeRequestStatus(booking.getId(), "CANCELED");
            });
        } catch (Exception e) {
            Log.wtf("ee", "error loading booking ui :" +e.getMessage());
        }

    }

    private void loadPendingRequestsIfExist(View view) {

        bookingApi.getBooking("PENDING").enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<List<Booking>> call, @NonNull Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    List<Booking> bookings = response.body();
                    if (bookings.isEmpty()){
                        showEmptyUi(view);
                    } else {
                        loadPendingRequestsUi(view, bookings);
                    }

                } else {
                    showEmptyUi(view);
                    Toast.makeText(getContext(), "No scheduleeeed ride found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(getContext(), "Failed to retrieve ride", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadPendingRequestsUi(View view, List<Booking> bookings) {
        LinearLayout layout = view.findViewById(R.id.pendingBookingsSection);
        layout.setVisibility(View.VISIBLE);

        RecyclerView pendingRecyclerView = view.findViewById(R.id.pendingBookingsRecycle); // use the correct ID

        BookingRequestAdapter bookingRequestAdapter = new BookingRequestAdapter(getContext(), bookings, new BookingRequestAdapter.OnBookingActionClick() {
            @Override
            public void onReject(Booking booking) {
                changeRequestStatus(booking.getId() ,"CANCELED");
            }
        });

        pendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingRecyclerView.setAdapter(bookingRequestAdapter);
    }

    private Button createBtn;
    private LinearLayout createLayout;
    private void showEmptyUi(View view) {
        // Set up the button click listener for navigating to create a ride
        createBtn = view.findViewById(R.id.btnCreateRide);
        createBtn.setOnClickListener(v -> navigateToCreateRide());
        createLayout = view.findViewById(R.id.createLayout);

        Toast.makeText(getContext(), "nothing to show", Toast.LENGTH_SHORT).show();


        executorService.submit(() -> {

            UserEntity userEntity = db.userDao().getUser();
            requireActivity().runOnUiThread(() -> {
                if (userEntity.role == UserEntity.Role.DRIVER) {
                    view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                        createLayout.setVisibility(View.VISIBLE);


                } else {
                    view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    TextView nothingText = view.findViewById(R.id.nothingTv);
                    nothingText.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void cancelRideDriverApi() {
        rideApi.changeRideStatus("CANCELED").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Ride cancelled successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Ride cancelled successfully!");
                    // Optionally, you can refresh your UI or go back
                } else {
                    Log.d(TAG, "Ride cancel failed: " + response.message());
                    Toast.makeText(getContext(), "Failed to cancel ride", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(getContext(), "Failed to cancel ride", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeRequestStatus(int id, String status) {
        bookingApi.changeRequestStatus(id, status)
                .enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.d("API_SUCCESS", "Response: success");

                            // Reload the fragment
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                    transaction.detach(BookingFragment.this).attach(BookingFragment.this).commit();
                                });
                            }

                        } else {
                            Log.e("API_ERROR", "Error: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("API_FAILURE", "Failure: " + t.getMessage());
                    }
                });
    }






    private void navigateToCreateRide(){
        Intent intent = new Intent(getActivity(), CreateRideActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .detach(this)
                .attach(this)
                .commit();
    }

    private void countdownTick(View view){
        // Countdown Section
        TextView countdownText = view.findViewById(R.id.countdownText);
        TextView countdownText2 = view.findViewById(R.id.rideHubcountdownText);

        LocalDateTime departureDateTime = null;
        Log.wtf("ee", "ride start time = " + ride.getDepartureTime());
        try {
            departureDateTime = LocalDateTime.parse(ride.getDepartureTime());
        } catch (Exception e) {
            Log.wtf("ee", "eorro countdown " + e.getMessage());
            e.printStackTrace();
        }

        if (departureDateTime != null) {
            LocalDateTime finalDepartureDateTime = departureDateTime;

            new CountDownTimer(Duration.between(LocalDateTime.now(), finalDepartureDateTime).toMillis(), 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    long seconds = millisUntilFinished / 1000;
                    long hours = seconds / 3600;
                    long minutes = (seconds % 3600) / 60;
                    long secs = seconds % 60;

                    String timeLeft = String.format("%02d:%02d:%02d", hours, minutes, secs);
                    countdownText.setText(timeLeft);
                    countdownText2.setText(timeLeft);
                }

                @Override
                public void onFinish() {
                    countdownText.setText("Ride Started!");
                    countdownText2.setText("Ride started");
                    if (user.getRole().equals(User.Role.DRIVER)){
                        Button startRideButton = view.findViewById(R.id.startRideButton);
                        startRideButton.setOnClickListener(v -> {
                            //todo startRide();
                        });
                    }
                }

            }.start();
        } else {
            countdownText.setText("Invalid time");
        }

    }




}
