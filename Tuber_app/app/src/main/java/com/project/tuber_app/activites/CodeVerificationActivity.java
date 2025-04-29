package com.project.tuber_app.activites;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.project.tuber_app.R;
import com.project.tuber_app.api.ApiClient;
import com.project.tuber_app.api.UserApi;
import com.project.tuber_app.databases.Database;
import com.project.tuber_app.entities.ResetPasswordRequest;

import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class CodeVerificationActivity extends AppCompatActivity {

        private EditText etDigit1, etDigit2, etDigit3, etDigit4, etDigit5, etDigit6, emailTv;
        private Button btnVerify, btnSend;
        private ProgressBar progressBar;
        private TextView tvMessage;
        private Database db;
        private String userEmail;  // Store the email here

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_verification_code);

            // Hide the action bar
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            // Get the email passed from the previous activity
            userEmail = getIntent().getStringExtra("USER_EMAIL");
            Log.wtf("ee", "sending emial"  + userEmail);

            if (userEmail == null || userEmail.isEmpty()){
                userEmail = null;
                emailTv = findViewById(R.id.emailTv);
                emailTv.setVisibility(View.VISIBLE);
            }

            // Initialize views
            etDigit1 = findViewById(R.id.etDigit1);
            etDigit2 = findViewById(R.id.etDigit2);
            etDigit3 = findViewById(R.id.etDigit3);
            etDigit4 = findViewById(R.id.etDigit4);
            etDigit5 = findViewById(R.id.etDigit5);
            etDigit6 = findViewById(R.id.etDigit6);
            btnVerify = findViewById(R.id.btnVerify);
            btnSend = findViewById(R.id.btnSend);
            progressBar = findViewById(R.id.progressBar);
            tvMessage = findViewById(R.id.tvMessage);

            // Set up digit input auto-focus
            setupVerificationCodeInput();
            if (userEmail != null && !userEmail.isEmpty()){
                sendEmail();
            }


            // Set up the button click listener for sending the code
            btnSend.setOnClickListener(v -> sendEmail());

            // Set up the button click listener for verifying the code
            btnVerify.setOnClickListener(v -> verifyCode());
        }


        private void sendEmail() {
            if (userEmail == null){
                userEmail = emailTv.getText().toString();
            }
            // Validate email
            if (TextUtils.isEmpty(userEmail)) {
                tvMessage.setText("Please enter your email.");
                tvMessage.setTextColor(ContextCompat.getColor(CodeVerificationActivity.this, android.R.color.holo_red_dark));
                tvMessage.setVisibility(View.VISIBLE);
                return;
            }
            Log.wtf("ee", "sending with email " + userEmail);

            // Show progress bar while sending email
            progressBar.setVisibility(View.VISIBLE);
            btnSend.setEnabled(false);
            btnVerify.setEnabled(false);

            // Make the network request to send the verification code
            UserApi userApi = ApiClient.getUserApi(this);
            Call<ResponseBody> call = userApi.sendVerificationCode(userEmail);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    progressBar.setVisibility(View.GONE);
                    btnSend.setEnabled(true);
                    btnVerify.setEnabled(true);

                    if (response.isSuccessful()) {
                        tvMessage.setText("Verification code sent to " + userEmail);
                        tvMessage.setTextColor(ContextCompat.getColor(CodeVerificationActivity.this, android.R.color.holo_green_dark));
                        // Focus on first digit input
                        etDigit1.requestFocus();
                    } else {
                        tvMessage.setText("Failed to send verification code: " + response.message());
                        tvMessage.setTextColor(ContextCompat.getColor(CodeVerificationActivity.this, android.R.color.holo_red_dark));
                    }
                    tvMessage.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    btnSend.setEnabled(true);
                    btnVerify.setEnabled(true);
                    tvMessage.setText("Error: " + t.getMessage());
                    tvMessage.setTextColor(ContextCompat.getColor(CodeVerificationActivity.this, android.R.color.holo_red_dark));
                    tvMessage.setVisibility(View.VISIBLE);
                }
            });
        }

    private void verifyCode() {
        if (userEmail == null || userEmail.isEmpty()) {
            tvMessage.setText("Please enter your email.");
            tvMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
            tvMessage.setVisibility(View.VISIBLE);
            return;
        }

        // Get the complete verification code
        String code = getVerificationCode();

        if (TextUtils.isEmpty(code) || code.length() < 6) {
            tvMessage.setText("Please enter the complete 6-digit code.");
            tvMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
            tvMessage.setVisibility(View.VISIBLE);
            return;
        }

        // Show the progress bar during verification
        progressBar.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);
        btnSend.setEnabled(false);
        tvMessage.setVisibility(View.GONE);

        UserApi userApi = ApiClient.getUserApi(getApplicationContext());
        Call<ResponseBody> call = userApi.verifyCodeEmail(userEmail, code);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                btnVerify.setEnabled(true);
                btnSend.setEnabled(true);

                if (response.isSuccessful()) {
                    tvMessage.setText("Verification successful!");
                    tvMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_green_dark));
                    navigate();
                } else {
                    tvMessage.setText("Invalid verification code. Please try again.");
                    tvMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
                }
                tvMessage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnVerify.setEnabled(true);
                btnSend.setEnabled(true);
                tvMessage.setText("Error: " + t.getMessage());
                tvMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
                tvMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    private void navigate() {
        // Retrieve the action type passed through the Intent
        String actionType = getIntent().getStringExtra("actionType");

        // Check if the actionType is passed and handle it accordingly
        if (actionType != null) {
            if (actionType.equals("RESET_PASSWORD")) {
                Log.d("CodeVerificationActivity", "Navigating to change password");

                // Start the ChangePasswordActivity
                Intent intent = new Intent(CodeVerificationActivity.this, ResetPasswordActivity.class);
                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
                finish();  // Optional: Close the current activity if no longer needed
            } else if (actionType.equals("RESET_EMAIL")){
                Intent intent = new Intent(CodeVerificationActivity.this, ResetEmailActivity.class);
                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
                finish();
            } else if (actionType.equals("CONFIRM_EMAIL")) {

                resetEmail(userEmail);
                // Clear all SharedPreferences
                getSharedPreferences("your_pref_name", MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply();

                // Optional: also clear Room database user data
                Executors.newSingleThreadExecutor().execute(() -> {
                    Database database = Database.getInstance(getApplicationContext());
                    database.userDao().clearUsers();
                });

                // Navigate to LoginActivity
                Intent intent = new Intent(CodeVerificationActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity history
                startActivity(intent);
                finish(); // Finish CodeVerificationActivity
            }

        } else {
            // If no action type is provided, you can handle this case here.
            Log.d("CodeVerificationActivity", "No action type passed with the intent.");
        }
    }


    private void setupVerificationCodeInput() {
        // Create array of digit EditTexts for easier handling
        final EditText[] digits = new EditText[]{
                etDigit1, etDigit2, etDigit3, etDigit4, etDigit5, etDigit6
        };

        // Add text change listeners to automatically move to next field
        for (int i = 0; i < digits.length - 1; i++) {
            final int currentIndex = i;
            digits[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not needed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Not needed
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1) {
                        // Move to next digit
                        digits[currentIndex + 1].requestFocus();
                    }
                }
            });
        }


        // Handle backspace to go to previous field
        for (int i = 1; i < digits.length; i++) {
            final int currentIndex = i;
            final int previousIndex = i - 1;

            digits[i].setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL &&
                            event.getAction() == KeyEvent.ACTION_DOWN &&
                            digits[currentIndex].getText().toString().isEmpty()) {
                        // Move to previous digit on backspace when current field is empty
                        digits[previousIndex].requestFocus();
                        return true;
                    }
                    return false;
                }
            });
        }

        // Set focus on first digit when screen loads
        digits[0].requestFocus();
    }

    private String getVerificationCode() {
        StringBuilder codeBuilder = new StringBuilder();
        codeBuilder.append(etDigit1.getText().toString());
        codeBuilder.append(etDigit2.getText().toString());
        codeBuilder.append(etDigit3.getText().toString());
        codeBuilder.append(etDigit4.getText().toString());
        codeBuilder.append(etDigit5.getText().toString());
        codeBuilder.append(etDigit6.getText().toString());
        return codeBuilder.toString();
    }

    private void resetEmail(String newEmail) {
        UserApi userApi = ApiClient.getUserApi(getApplicationContext());
        Call<ResponseBody> call = userApi.resetEmail(new ResetPasswordRequest(newEmail, null));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(CodeVerificationActivity.this, "Email updated successfully. Please log in again.", Toast.LENGTH_SHORT).show();

                    // Clear all shared preferences
                    SharedPreferences preferences = getSharedPreferences("auth", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();

                    // Go back to LoginActivity
                    Intent intent = new Intent(CodeVerificationActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears back stack
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CodeVerificationActivity.this, "Failed to update email: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CodeVerificationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}