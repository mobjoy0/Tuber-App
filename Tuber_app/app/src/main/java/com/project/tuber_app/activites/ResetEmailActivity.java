package com.project.tuber_app.activites;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.project.tuber_app.R;
import com.project.tuber_app.api.ApiClient;
import com.project.tuber_app.api.UserApi;
import com.project.tuber_app.databases.Location;
import com.project.tuber_app.entities.ResetPasswordRequest;

import android.widget.ProgressBar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ResetEmailActivity extends AppCompatActivity {
    private TextInputEditText newEmail, confirmEmail;
    private TextInputLayout newPasswordLayout, confirmPasswordLayout;
    private ProgressBar progressBar;
    private MaterialButton updatePasswordButton;
    private Button cancelButton;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changeemail);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize views
        newEmail = findViewById(R.id.newEmail);
        confirmEmail = findViewById(R.id.confirmEmail);
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
                String newEmail = ResetEmailActivity.this.newEmail.getText().toString().trim();
                String confirmEmail = ResetEmailActivity.this.confirmEmail.getText().toString().trim();

                if (TextUtils.isEmpty(newEmail)) {
                    newPasswordLayout.setError("Email cannot be empty");
                    return;
                }

                if (TextUtils.isEmpty(confirmEmail)) {
                    confirmPasswordLayout.setError("Please confirm your email");
                    return;
                }

                if (!newEmail.equals(confirmEmail)) {
                    confirmPasswordLayout.setError("email do not match");
                    return;
                }


                // Show the progress bar
                progressBar.setVisibility(View.VISIBLE);

                // Make API call to reset password
                navigateToVerify(newEmail);
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

    private void navigateToVerify(String newEmail){
        Intent intent = new Intent(ResetEmailActivity.this, CodeVerificationActivity.class);
        Log.wtf("ee", "sending emial"  + newEmail);
        intent.putExtra("USER_EMAIL", newEmail);
        intent.putExtra("actionType", "CONFIRM_EMAIL");
        startActivity(intent);
    }


}
