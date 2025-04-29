package com.project.tuber_app.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.tuber_app.R;
import com.project.tuber_app.adapters.RideAdapter;
import com.project.tuber_app.entities.User;
import com.project.tuber_app.api.ApiClient;
import com.project.tuber_app.api.RideApi;
import com.project.tuber_app.databases.Database;
import com.project.tuber_app.databases.Location;
import com.project.tuber_app.databases.UserDao;
import com.project.tuber_app.entities.Ride;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private AutoCompleteTextView fromInput;
    private AutoCompleteTextView toInput;
    private MaterialButton searchButton;
    private MaterialCardView dateCard;
    private TextView datePickerText;
    private MaterialCardView timeCard;
    private TextView timePickerText;
    private FloatingActionButton swapButton;
    private RadioGroup dateRadioGroup;
    private RadioButton radioToday;
    private RadioButton radioTomorrow;
    private RadioButton radioCustom;

    private Calendar selectedDateTime;
    private SimpleDateFormat displayDateFormat;
    private SimpleDateFormat displayTimeFormat;
    private SimpleDateFormat apiDateTimeFormat;

    // Sample locations for autocomplete

    private static final String TAG = "HomeFragment";
    private User user;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize date formats
        displayDateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        displayTimeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        apiDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        // Initialize calendar with current date and time
        selectedDateTime = Calendar.getInstance();

        // Find views
        fromInput = view.findViewById(R.id.from_input);
        toInput = view.findViewById(R.id.to_input);
        searchButton = view.findViewById(R.id.search_button);
        dateCard = view.findViewById(R.id.date_card);
        datePickerText = view.findViewById(R.id.date_picker);
        timeCard = view.findViewById(R.id.time_card);
        timePickerText = view.findViewById(R.id.time_picker);
        swapButton = view.findViewById(R.id.swap_button);
        dateRadioGroup = view.findViewById(R.id.date_radio_group);
        radioToday = view.findViewById(R.id.radio_today);
        radioTomorrow = view.findViewById(R.id.radio_tomorrow);
        radioCustom = view.findViewById(R.id.radio_custom);

        // Set up autocomplete for location inputs
        setupLocationAutocomplete();
        recommendedRides(view);

        // Update date and time displays
        updateDateDisplay();
        updateTimeDisplay();

        // Set up date picker
        dateCard.setOnClickListener(v -> {
            radioCustom.setChecked(true);
            showDatePicker();
        });

        // Set up time picker
        timeCard.setOnClickListener(v -> showTimePicker());

        // Set up radio group listener
        dateRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_today) {
                // Set date to today
                Calendar now = Calendar.getInstance();
                selectedDateTime.set(Calendar.YEAR, now.get(Calendar.YEAR));
                selectedDateTime.set(Calendar.MONTH, now.get(Calendar.MONTH));
                selectedDateTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
                updateDateDisplay();
            } else if (checkedId == R.id.radio_tomorrow) {
                // Set date to tomorrow
                Calendar tomorrow = Calendar.getInstance();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                selectedDateTime.set(Calendar.YEAR, tomorrow.get(Calendar.YEAR));
                selectedDateTime.set(Calendar.MONTH, tomorrow.get(Calendar.MONTH));
                selectedDateTime.set(Calendar.DAY_OF_MONTH, tomorrow.get(Calendar.DAY_OF_MONTH));
                updateDateDisplay();
            }
        });

        // Set up location swap functionality
        swapButton.setOnClickListener(v -> swapLocations());

        // Set up search button
        searchButton.setOnClickListener(v -> {
            String from = fromInput.getText() != null ? fromInput.getText().toString().trim() : "";
            String to = toInput.getText() != null ? toInput.getText().toString().trim() : "";

            if (from.isEmpty() || to.isEmpty()) {
                Toast.makeText(getContext(), "Please fill both location fields", Toast.LENGTH_SHORT).show();
            } else {
                searchRides(from, to);
            }
        });
    }

    private void setupLocationAutocomplete() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line
        );

        fromInput.setAdapter(adapter);
        toInput.setAdapter(adapter);


        Database db = Database.getInstance(requireContext());
        executorService.submit(() -> {
            List<String> locations = db.locationsDao().getAllLocations();
            requireActivity().runOnUiThread(() -> {
                 ArrayAdapter<String> dbAdapter = new ArrayAdapter<>(
                         requireContext(),
                         android.R.layout.simple_dropdown_item_1line,
                         locations
                 );
                 fromInput.setAdapter(dbAdapter);
                 toInput.setAdapter(dbAdapter);
             });
        });
    }

    private void swapLocations() {
        CharSequence fromText = fromInput.getText();
        CharSequence toText = toInput.getText();

        fromInput.setText(toText);
        toInput.setText(fromText);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                false
        );

        timePickerDialog.show();
    }

    private void updateDateDisplay() {
        // Format current date for display
        String formattedDate = displayDateFormat.format(selectedDateTime.getTime());

        // Check if it's today
        Calendar today = Calendar.getInstance();
        if (selectedDateTime.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                selectedDateTime.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            formattedDate = "Today, " + formattedDate;
            if (!radioToday.isChecked()) {
                radioToday.setChecked(true);
            }
        }
        // Check if it's tomorrow
        else {
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);
            if (selectedDateTime.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                    selectedDateTime.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)) {
                formattedDate = "Tomorrow, " + formattedDate;
                if (!radioTomorrow.isChecked()) {
                    radioTomorrow.setChecked(true);
                }
            } else if (!radioCustom.isChecked()) {
                radioCustom.setChecked(true);
            }
        }

        datePickerText.setText(formattedDate);
    }

    private void updateTimeDisplay() {
        // Check if the time is approximately now (within 5 minutes)
        Calendar now = Calendar.getInstance();
        long diffInMillis = Math.abs(selectedDateTime.getTimeInMillis() - now.getTimeInMillis());
        long diffInMinutes = diffInMillis / (60 * 1000);

        if (diffInMinutes < 5) {
            timePickerText.setText("Now");
        } else {
            String formattedTime = displayTimeFormat.format(selectedDateTime.getTime());
            timePickerText.setText(formattedTime);
        }
    }

    private void searchRides(String origin, String destination) {
        Log.d(TAG, "Searching for rides from " + origin + " to " + destination);

        RideApi api = ApiClient.getRideApi(getContext());

        // Format date for API
        String dateTimeForApi = apiDateTimeFormat.format(selectedDateTime.getTime());
        Log.d(TAG, "Using date/time for API search: " + dateTimeForApi);



        // Optional price parameter (null for no limit)
        BigDecimal maxPrice = null;

        Call<List<Ride>> call = api.searchRides(origin, destination, dateTimeForApi);

        call.enqueue(new Callback<List<Ride>>() {
            @Override
            public void onResponse(Call<List<Ride>> call, Response<List<Ride>> response) {
                Log.d(TAG, "API response received");

                if (response.isSuccessful() && response.body() != null) {
                    List<Ride> rides = response.body();
                    Log.d(TAG, "Found " + rides.size() + " rides");

                    if (rides.isEmpty()) {
                        Toast.makeText(getContext(), "No rides found for your search criteria.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Found " + rides.size() + " rides", Toast.LENGTH_SHORT).show();
                        navigateToRideResults(rides);
                    }
                } else {
                    Log.e(TAG, "Response unsuccessful: " + response.code());
                    Toast.makeText(getContext(), "Failed to load rides. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Ride>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToRideResults(List<Ride> rideList) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("ride_list", new ArrayList<>(rideList));

        RideResultsFragment fragment = new RideResultsFragment();
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit();
    }

    RideAdapter rideAdapter;

    private void recommendedRides(View view) {
        RecyclerView ridesRecyclerView = view.findViewById(R.id.recommandedRidesRecycleView);
        ridesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));


        // Make the API call
        RideApi rideApi = ApiClient.getRideApi(requireContext());
        rideApi.getRideRecommendations().enqueue(new Callback<List<Ride>>() {
            @Override
            public void onResponse(Call<List<Ride>> call, Response<List<Ride>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Ride> rideList = response.body();// Update RecyclerView with the ride list
                    if (rideList != null && !rideList.isEmpty()) {
                        view.findViewById(R.id.empty_state).setVisibility(View.GONE);
                        rideAdapter = new RideAdapter(rideList, ride -> {
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("selected_ride", ride);

                            RideDetailsFragment detailFragment = new RideDetailsFragment();
                            detailFragment.setArguments(bundle);

                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.frameLayout, detailFragment)
                                    .addToBackStack(null)
                                    .commit();
                        });
                        ridesRecyclerView.setAdapter(rideAdapter);
                    } else {
                        ridesRecyclerView.setVisibility(View.GONE);
                        view.findViewById(R.id.empty_state).setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), "No rides found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load rides", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Ride>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // No need to shutdown executor service here as it's a class member
        // and may be needed if the fragment is recreated
    }
}