package com.project.tuber_app.activites;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.project.tuber_app.R;
import com.project.tuber_app.api.ApiClient;
import com.project.tuber_app.api.LoginApi;
import com.project.tuber_app.entities.User;
import com.project.tuber_app.entities.User.Gender;
import com.project.tuber_app.entities.User.Role;

import java.text.SimpleDateFormat;
import java.util.Date;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {

    EditText firstNameEditText, lastNameEditText, emailEditText, phoneNumberEditText,
            cinEditText, passwordEditText, confirmPasswordEditText, birthDateEditText;
    RadioGroup genderRadioGroup, roleRadioGroup;
    Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize UI elements
        firstNameEditText = findViewById(R.id.editTextFirstName);
        lastNameEditText = findViewById(R.id.editTextLastName);
        emailEditText = findViewById(R.id.editTextEmail);
        phoneNumberEditText = findViewById(R.id.editTextPhoneNumber);
        cinEditText = findViewById(R.id.editTextCIN);
        passwordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        birthDateEditText = findViewById(R.id.editTextBirthDate);
        genderRadioGroup = findViewById(R.id.radioGroupGender);
        roleRadioGroup = findViewById(R.id.radioGroupRole);
        registerButton = findViewById(R.id.buttonRegister);


        // Set click listener for register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gather inputs
                String firstName = firstNameEditText.getText().toString().trim();
                String lastName = lastNameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String phoneNumber = phoneNumberEditText.getText().toString().trim();
                String cin = cinEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();
                String birthDateString = birthDateEditText.getText().toString().trim();

                // Validate inputs
                if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) ||
                        TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(cin) || TextUtils.isEmpty(password) ||
                        TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(birthDateString)) {
                    Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if passwords match
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get selected gender and role
                int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
                RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
                Gender gender = selectedGenderRadioButton != null && selectedGenderRadioButton.getText().toString().equals("Male") ? Gender.MALE : Gender.FEMALE;

                // Get selected role
                int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();
                RadioButton selectedRoleRadioButton = findViewById(selectedRoleId);

// Assuming the backend expects 'DRIVER' or 'RIDER'
                Role role = selectedRoleRadioButton != null && selectedRoleRadioButton.getText().toString().equals("Driver") ? Role.DRIVER : Role.RIDER;

                // Convert birthDate string to Date
                String birthDate;
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(birthDateString); // Parse string → Date
                    birthDate = sdf.format(date); // Format Date → String (correct way)
                    Log.d("DATE", "Formatted date: " + birthDate); // Verify output
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(RegisterActivity.this, "Invalid birth date format. Use yyyy-MM-dd.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create user data object
                User user = new User(firstName, lastName, email, phoneNumber, cin, password, gender, birthDate, role);
                Log.d("DATE", "error: " + user.toString()); // Verify output


                // Send data to the server
                LoginApi api = ApiClient.getLoginApi(getApplicationContext());
                Call<Void > call = api.registerUser(user);
                call.enqueue(new Callback<>() {
                    @Override

                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            try {
                                String errorBody = response.errorBody().string();  // Get error details
                                Log.e("RegisterError", "Failed with code: " + response.code() + ", Response: " + errorBody);

                                Toast.makeText(RegisterActivity.this, "Error: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (t instanceof java.net.UnknownHostException) {
                            Toast.makeText(RegisterActivity.this, "No internet connection. Please check your connection.", Toast.LENGTH_SHORT).show();
                            Log.e("RegisterError", "No Internet Connection: " + t.getMessage());
                        } else if (t instanceof java.net.SocketTimeoutException) {
                            Toast.makeText(RegisterActivity.this, "Connection timed out. Try again.", Toast.LENGTH_SHORT).show();
                            Log.e("RegisterError", "Timeout: " + t.getMessage());
                        } else if (t instanceof java.net.ConnectException) {
                            Toast.makeText(RegisterActivity.this, "Could not connect to server. Please try again later.", Toast.LENGTH_SHORT).show();
                            Log.e("RegisterError", "Server Unreachable: " + t.getMessage());
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("RegisterError", "Other Error: " + t.getMessage());
                        }
                    }
                });
            }
        });
    }
}
