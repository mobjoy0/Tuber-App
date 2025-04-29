package com.project.tuber_app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.project.tuber_app.R;
import com.project.tuber_app.entities.User;
import com.project.tuber_app.api.ApiClient;
import com.project.tuber_app.api.BookingApi;
import com.project.tuber_app.databases.Database;
import com.project.tuber_app.databases.UserDao;
import com.project.tuber_app.entities.Booking;
import com.project.tuber_app.entities.Ride;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideDetailsFragment extends Fragment {

    private Ride selectedRide;
    private int selectedSeats = 1;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            selectedRide = getArguments().getParcelable("selected_ride");
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_ride_details, container, false);

        if (selectedRide != null) {
            TextView fromText = view.findViewById(R.id.fromTextView);
            TextView toText = view.findViewById(R.id.destinationTextView);
            TextView priceText = view.findViewById(R.id.priceTextView);
            TextView departureTimeText = view.findViewById(R.id.DepartureTimeTextView);
            TextView arrivalTimeText = view.findViewById(R.id.DestinationTimeTextView);
            TextView rideTimeText = view.findViewById(R.id.RideTimeTextView);
            TextView distanceText = view.findViewById(R.id.distanceTextView);
            NumberPicker seatPicker = view.findViewById(R.id.seatPicker);
            Button confirmButton = view.findViewById(R.id.confirmButton);
            try {
                fromText.setText(selectedRide.getStartLocation());
                toText.setText(selectedRide.getEndLocation());
                departureTimeText.setText(selectedRide.getRideTime());
                arrivalTimeText.setText(selectedRide.getRideEndTime());
                rideTimeText.setText(selectedRide.getRideDuration());
                distanceText.setText(selectedRide.getDistance() + "km");

                // Setup seat picker
                seatPicker.setMinValue(1);
                seatPicker.setMaxValue(selectedRide.getAvailableSeats());
                seatPicker.setValue(1);
                seatPicker.setWrapSelectorWheel(false);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            // Set initial price
            double initialPrice = selectedRide.getPrice();
            priceText.setText(initialPrice + " dt");

            // Listen for seat changes
            seatPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                selectedSeats = newVal;
                double totalPrice = selectedRide.getPrice() * newVal;
                priceText.setText(totalPrice + " dt");
            });

            // Confirm button logic
            confirmButton.setOnClickListener(v -> {
                Database db = Database.getInstance(getContext());
                UserDao userDao = db.userDao();

                executorService.submit(() -> {
                    try {
                        User user = new User(userDao.getUser());
                        Booking booking = new Booking(0, selectedRide, user, seatPicker.getValue());

                        BookingApi apiService = ApiClient.getBookingApi(getContext());
                        Call<Void> call = apiService.createBooking(booking);

                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(getContext(), "Booking confirmed!", Toast.LENGTH_SHORT).show()
                                    );
                                    navigateToHome();
                                } else {
                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(getContext(), "Booking failed: " + response.code(), Toast.LENGTH_SHORT).show()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                            }
                        });

                    } catch (Exception e) {
                        Log.e("RoomInsertError", "Error inserting user: ", e);
                    }
                });
            });



        }

        return view;
    }


    private void navigateToHome() {

        HomeFragment fragment = new HomeFragment();

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit();
    }
}
