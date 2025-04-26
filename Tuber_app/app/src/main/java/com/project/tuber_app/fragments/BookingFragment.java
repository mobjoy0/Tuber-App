package com.project.tuber_app.fragments;

import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.project.tuber_app.R;
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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public BookingFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Database db = Database.getInstance(getContext());
        rideApi = ApiClient.getRideApi(getContext());
        bookingApi = ApiClient.getBookingApi(getContext());

        executorService.submit(() -> {
            UserEntity userEntity = db.userDao().getUser();

            requireActivity().runOnUiThread(() -> {
                try {
                    if (userEntity.role == UserEntity.Role.DRIVER) {
                        Toast.makeText(getContext(), "Fetching ride data as driver...", Toast.LENGTH_SHORT).show();
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
    }

    public void retrieveRideIfExists(View view) {
        rideApi.getRideByDriver().enqueue(new Callback<Ride>() {
            @Override
            public void onResponse(@NonNull Call<Ride> call, @NonNull Response<Ride> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ride = response.body();
                    Log.d(TAG, "Scheduled ride found: " + ride);

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
    }

    private void fetchConfirmedBooking(View view, int rideId) {
        bookingApi.getConfirmedBooking(rideId).enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<List<Booking>> call, @NonNull Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    confirmedRecyclerView = view.findViewById(R.id.confirmedPassagersRecycle);

                    confirmedBooking = response.body();
                    Log.wtf("ee", "confiremd passagers = " + confirmedBooking.size());
                    confirmedAdapter = new ConfirmedRequestAdapter(confirmedBooking);
                    confirmedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    confirmedRecyclerView.setAdapter(confirmedAdapter);
                } else {
                    Toast.makeText(getContext(), "No confirmed passagers bookings", Toast.LENGTH_SHORT).show();
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

                    pendingBooking = response.body();
                    Log.wtf("ee", "pending passagers = " + pendingBooking.size());
                    pendingAdapter = new PendingRequestAdapter(pendingBooking, new PendingRequestAdapter.OnBookingActionClick() {
                        @Override
                        public void onAccept(Booking booking) {
                            // TODO: implement accept logic
                            Toast.makeText(getContext(), "Accepted: " + booking.getId(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReject(Booking booking) {
                            // TODO: implement reject logic
                            Toast.makeText(getContext(), "Rejected: " + booking.getId(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    pendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    pendingRecyclerView.setAdapter(pendingAdapter);
                } else {
                    Toast.makeText(getContext(), "No pending requests bookings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to fetch pending bookings", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void retrieveConfirmedBookingIfExists(View view){
        bookingApi.getBooking("CONFIRMED").enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<List<Booking>> call, @NonNull Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body();
                    Log.d(TAG, "confirmed booking found: " + bookings);

                    LoadRideDetailsSectionUI(booking, view);
                } else {

                    //TODO: ui for when user has no confirmed booking *fuction* so it can be reused for when the driver has no ride
                    Log.d(TAG, " non confirmed booking found");
                    loadPendingRequestsIfExist(view);

                    Toast.makeText(getContext(), "No confirmed ride found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(getContext(), "Failed to retrieve ride", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void LoadRideDetailsSectionUI(Booking booking, View view) {
        // Get the root layout for the section
        try {
            CardView rideDetailsSection = view.findViewById(R.id.rideDetailsCard);
            rideDetailsSection.setVisibility(View.VISIBLE); // Make it visible if hidden

            // Departure Section
            TextView departureText = view.findViewById(R.id.departureText);
            TextView departureTime = view.findViewById(R.id.departureTime);
            departureText.setText(booking.getRide().getStartLocation()); // Assuming `getDepartureLocation()` method
            departureTime.setText(booking.getRide().getDepartureTime().toString()); // Assuming `getDepartureTime()` is LocalDateTime

            // Arrival Section
            TextView arrivalText = view.findViewById(R.id.arrivalText);
            TextView arrivalTime = view.findViewById(R.id.arrivalTime);
            arrivalText.setText(booking.getRide().getEndLocation()); // Assuming `getArrivalLocation()`
            arrivalTime.setText(booking.getRide().getDepartureTime().toString()); // Assuming `getArrivalTime()` is LocalDateTime

            // Distance Section
            TextView distanceValue = view.findViewById(R.id.distanceValue);
            distanceValue.setText(booking.getRide().getDistance() + " km"); // Assuming `getDistance()` returns distance

            // Duration Section
            TextView durationValue = view.findViewById(R.id.durationValue);
            //durationValue.setText(booking.getRide().getEta()); // Assuming `getDuration()` returns duration as string (e.g. "1h 32m")

            // Booking Summary Section
            TextView seatSummaryText = view.findViewById(R.id.seatSummaryText);
            seatSummaryText.setText("You have booked " + booking.getSeatsBooked() + " seats for " + booking.getRide().getPrice() * booking.getSeatsBooked() + "dt."); // Assuming `getPrice()` returns price

            // Countdown Section
            TextView countdownText = view.findViewById(R.id.countdownText);
            // Set countdown logic here (for example, use a timer or calculate based on ride time)
            countdownText.setText("Countdown here"); // This could be dynamic, such as calculating the time difference

            // Cancel Button Section
            Button cancelButton = view.findViewById(R.id.cancelButton);
            cancelButton.setOnClickListener(v -> {
                // Handle ride cancellation logic here
                // For example, change the booking status to cancelled
                booking.setStatus(Booking.Status.CANCELED);
                // Call your API to update status if necessary
                Toast.makeText(getContext(), "Ride has been cancelled.", Toast.LENGTH_SHORT).show();
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
                    List<Booking> bookings = response.body();
                    if (bookings.isEmpty()){
                        showEmptyUi(view);
                    } else {
                        loadPendingRequestsUi(view, bookings);
                    }
                    Log.d(TAG, "pending booking found: " + bookings.size());

                } else {
                    Log.d(TAG, "No pending booking found");
                    showEmptyUi(view);
                    Toast.makeText(getContext(), "No scheduled ride found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(getContext(), "Failed to retrieve ride", Toast.LENGTH_SHORT).show();
            }
        });
    }

    RecyclerView pendingBookingsRecycle;
    BookingRequestAdapter bookingRequestAdapter;

    private void loadPendingRequestsUi(View view, List<Booking> bookings) {
        LinearLayout layout = view.findViewById(R.id.pendingBookingsSection);
        layout.setVisibility(View.VISIBLE);

        RecyclerView pendingRecyclerView = view.findViewById(R.id.pendingBookingsRecycle); // use the correct ID

        BookingRequestAdapter bookingRequestAdapter = new BookingRequestAdapter(getContext(), bookings, new BookingRequestAdapter.OnBookingActionClick() {
            @Override
            public void onReject(Booking booking) {
                Toast.makeText(getContext(), "Rejected: " + booking.getId(), Toast.LENGTH_SHORT).show();
            }
        });

        pendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingRecyclerView.setAdapter(bookingRequestAdapter);
    }


    private void showEmptyUi(View view){
        TextView nothingText = view.findViewById(R.id.nothingTv);
        nothingText.setVisibility(View.VISIBLE);
    }

}
