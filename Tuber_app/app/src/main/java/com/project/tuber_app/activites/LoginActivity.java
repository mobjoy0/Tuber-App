package com.project.tuber_app.activites;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.project.tuber_app.R;
import com.project.tuber_app.api.ApiClient;
import com.project.tuber_app.api.LoginApi;
import com.project.tuber_app.databases.Database;
import com.project.tuber_app.databases.UserDao;
import com.project.tuber_app.databases.UserEntity;
import com.project.tuber_app.entities.User;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.Intent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    EditText emailEditText, passwordEditText;
    Button loginButton;
    ApiClient apiClient;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);



        // Set click listener on login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                Log.wtf("LoginDebug", "Attempting login with email: " + email);
                loginRequestCall(email, password);


            }
        });
        Button createAccountButton = findViewById(R.id.createAccountButton);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    public void loginRequestCall(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);

        LoginApi api = ApiClient.getLoginApi(getApplicationContext());

        Call<LoginResponse> call = api.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    User user = response.body().getUser();

                    Log.d("Login", "Token: " + token);
                    Log.d("Login", "UserID: " + user.getId());

                    // Save token in SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE);
                    sharedPreferences.edit().putString("jwt_token", token).apply();

                    // Save user in local DB
                    Database db = Database.getInstance(getApplicationContext());
                    UserDao userDao = db.userDao();

                    UserEntity userEntity = new UserEntity(user);
                    Log.wtf("e", "userdata ="+userEntity.cin);
                    executorService.submit(() -> {
                        try {
                            userDao.clearUsers();
                            userDao.insertUser(userEntity);
                            Log.d("RoomInsert", "User inserted successfully");
                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            });
                        } catch (Exception e) {
                            Log.e("RoomInsertError", "Error inserting user: ", e);
                        }
                    });


                } else {
                    Toast.makeText(LoginActivity.this, "Login failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("Login", "Response not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("LoginError", "Error: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
