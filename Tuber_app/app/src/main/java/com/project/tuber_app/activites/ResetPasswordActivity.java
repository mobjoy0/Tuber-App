package com.project.tuber_app.activites;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.project.tuber_app.R;
import com.project.tuber_app.api.ApiClient;
import com.project.tuber_app.api.UserApi;
import com.project.tuber_app.entities.ResetPasswordRequest;

import android.widget.ProgressBar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ResetPasswordActivity extends AppCompatActivity {
    private TextInputEditText newPasswordEditText, confirmPasswordEditText;
    private TextInputLayout newPasswordLayout, confirmPasswordLayout;
    private ProgressBar progressBar;
    private MaterialButton updatePasswordButton;
    private Button cancelButton;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null || userEmail.isEmpty()){
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // Initialize views
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        progressBar = findViewById(R.id.progressBar);
        updatePasswordButton = findViewById(R.id.updatePasswordButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Update Password Button Click Listener
        updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate the input
                String newPassword = newPasswordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(newPassword)) {
                    newPasswordLayout.setError("Password cannot be empty");
                    return;
                }

                if (TextUtils.isEmpty(confirmPassword)) {
                    confirmPasswordLayout.setError("Please confirm your password");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    confirmPasswordLayout.setError("Passwords do not match");
                    return;
                }

                if (newPassword.length() < 8) {
                    newPasswordLayout.setError("Password must be at least 8 characters long");
                    return;
                }

                // Show the progress bar
                progressBar.setVisibility(View.VISIBLE);

                // Make API call to reset password
                resetPassword(newPassword);
            }
        });

        // Cancel Button Click Listener
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity
            }
        });
    }

    private void resetPassword(String newPassword) {
        // Replace this with your API client and endpoint to reset the password
        UserApi userApi = ApiClient.getUserApi(getApplicationContext());
        Call<ResponseBody> call = userApi.resetPassword(new ResetPasswordRequest(userEmail, newPassword));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    // Handle success
                    Toast.makeText(ResetPasswordActivity.this, "Password reset successful", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                } else {
                    // Handle failure (show error)
                    Toast.makeText(ResetPasswordActivity.this, "Failed to reset password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                // Handle error (network failure, etc.)
                Toast.makeText(ResetPasswordActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
