package com.project.tuber_app.activites;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.project.tuber_app.R;
import com.project.tuber_app.api.ApiClient;
import com.project.tuber_app.api.RideApi;
import com.project.tuber_app.databases.Database;
import com.project.tuber_app.databases.UserEntity;
import com.project.tuber_app.entities.Ride;
import com.project.tuber_app.entities.User;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateRideActivity extends AppCompatActivity {

    private AutoCompleteTextView tilDepartureLocation, tilArrivalLocation;
    private TextInputEditText etDepartureTime, etAvailableSeats, etPrice;
    private MaterialButton btnCreateRide;
    private View progressBar;
    private Calendar selectedDateTime = Calendar.getInstance();
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ride);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize views
        tilDepartureLocation = findViewById(R.id.tilDepartureLocation);
        tilArrivalLocation = findViewById(R.id.tilArrivalLocation);
        etDepartureTime = findViewById(R.id.etDepartureTime);
        etAvailableSeats = findViewById(R.id.etAvailableSeats);
        etPrice = findViewById(R.id.etPrice);
        btnCreateRide = findViewById(R.id.btnCreateRide);
        progressBar = findViewById(R.id.progressBar);

        // Initialize location autocomplete manager
        setupLocationAutocomplete();

        initializeDateTimePicker();
        setupCreateRideButton();
    }

    private void setupLocationAutocomplete() {
        Database db = Database.getInstance(getApplicationContext());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            List<String> locations = db.locationsDao().getAllLocations();
            runOnUiThread(() -> {
                ArrayAdapter<String> dbAdapter = new ArrayAdapter<>(
                        getApplicationContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        locations
                );
                tilDepartureLocation.setAdapter(dbAdapter);
                tilArrivalLocation.setAdapter(dbAdapter);
            });
        });
    }


    private void initializeDateTimePicker() {
        etDepartureTime.setOnClickListener(v -> showDateTimePicker());
    }

    private void showDateTimePicker() {
        // Get current date
        final Calendar currentDate = Calendar.getInstance();

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // After date is selected, show time picker
                    showTimePicker();
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(currentDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        // Get current time
        final Calendar currentTime = Calendar.getInstance();

        // Create TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);

                    // Update the EditText with selected date and time
                    etDepartureTime.setText(dateTimeFormat.format(selectedDateTime.getTime()));
                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                true // 24-hour format
        );

        timePickerDialog.show();
    }

    private void setupCreateRideButton() {
        btnCreateRide.setOnClickListener(v -> {
            if (validateInputs()) {
                createRide();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String departureLocation = tilDepartureLocation.getText().toString();
        String departureTime = etDepartureTime.getText().toString().trim();
        String arrivalLocation = tilArrivalLocation.getText().toString();
        String availableSeats = etAvailableSeats.getText().toString().trim();

        if (TextUtils.isEmpty(departureLocation)) {
            Toast.makeText(this, "Departure location is required", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (TextUtils.isEmpty(departureTime)) {
            etDepartureTime.setError("Departure time is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(arrivalLocation)) {
            Toast.makeText(this, "Arrival location is required", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (TextUtils.isEmpty(availableSeats)) {
            etAvailableSeats.setError("Available seats is required");
            isValid = false;
        } else {
            try {
                int seats = Integer.parseInt(availableSeats);
                if (seats <= 0 || seats > 20) {
                    etAvailableSeats.setError("Please enter a valid number of seats (1-20)");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etAvailableSeats.setError("Please enter a valid number");
                isValid = false;
            }
        }

        return isValid;
    }

    private Database database;
    private UserEntity userEntity;

    private void createRide() {
        progressBar.setVisibility(View.VISIBLE);
        btnCreateRide.setEnabled(false);
        database = Database.getInstance(getApplicationContext());

        Executors.newSingleThreadExecutor().execute(() -> {
            userEntity = database.userDao().getUser();

            runOnUiThread(() -> {
                if (userEntity == null) {
                    Log.e("CreateRide", "UserEntity is null!");
                    progressBar.setVisibility(View.GONE);
                    btnCreateRide.setEnabled(true);
                    return;
                }
                Log.wtf("ee", "User info = " + userEntity.id);

                Ride rideRequest = new Ride();
                rideRequest.setDriver(new User(userEntity));

                rideRequest.setStartLocation(tilDepartureLocation.getText().toString());
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                rideRequest.setDepartureTime(isoFormat.format(selectedDateTime.getTime()));
                rideRequest.setEndLocation(tilArrivalLocation.getText().toString());
                rideRequest.setAvailableSeats(Integer.parseInt(etAvailableSeats.getText().toString().trim()));
                rideRequest.setTotalSeats(Integer.parseInt(etAvailableSeats.getText().toString().trim()));
                String priceStr = etPrice.getText().toString().trim();
                if (!TextUtils.isEmpty(priceStr)) {
                    rideRequest.setPrice(Double.parseDouble(priceStr));
                }
                Log.wtf("e","ride is "+ rideRequest.toString());

                RideApi rideApi = ApiClient.getRideApi(CreateRideActivity.this);
                Call<ResponseBody> call = rideApi.createRide(rideRequest);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        progressBar.setVisibility(View.GONE);
                        btnCreateRide.setEnabled(true);

                        if (response.isSuccessful()) {
                            Toast.makeText(CreateRideActivity.this, "Ride created successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CreateRideActivity.this,
                                    "Failed to create ride: " + response.message(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnCreateRide.setEnabled(true);
                        Toast.makeText(CreateRideActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
    }
}