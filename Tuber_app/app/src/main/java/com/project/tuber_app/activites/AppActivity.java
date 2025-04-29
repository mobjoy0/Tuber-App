package com.project.tuber_app.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.project.tuber_app.R;
import com.project.tuber_app.api.ApiClient;
import com.project.tuber_app.api.UserApi;
import com.project.tuber_app.databases.Database;
import com.project.tuber_app.databases.Location;
import com.project.tuber_app.databases.UserEntity;
import com.project.tuber_app.entities.User;

import org.json.JSONObject;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppActivity extends AppCompatActivity {

    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        database = Database.getInstance(getApplicationContext());

        loadLocations();

        new Handler().postDelayed(this::checkLoginStatus, 1500);
    }

    private void checkLoginStatus() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);


        if (isTokenValid(token)) {
            Log.wtf("e", "token is valid");
            if (isNetworkAvailable()) {
                Log.wtf("e", "network avaivlabe");

                updateUserData();
            } else {
                goToLogin();
            }
        } else {
            Log.wtf("e", "token not valid");
            goToLogin();
        }
    }


    private boolean isTokenValid(String token) {
        try {
            if (token == null || token.isEmpty()) return false;

            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String payloadJson = new String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8);
            JSONObject payload = new JSONObject(payloadJson);

            long exp = payload.getLong("exp");
            long now = System.currentTimeMillis() / 1000L;

            return exp > now;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateUserData() {
        UserApi userApi = ApiClient.getUserApi(getApplicationContext());

        Call<User> call = userApi.getUserProfile();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();

                    // Save updated user to database
                    Executors.newSingleThreadExecutor().execute(() -> {
                        database.userDao().clearUsers();
                        database.userDao().insertUser(new UserEntity(user));

                        runOnUiThread(() -> {
                            Intent intent = new Intent(AppActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    });
                } else {
                    Log.wtf("e", "error is updaitng");
                    goToLogin();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                t.printStackTrace();
                goToLogin();
            }
        });
    }

    private void goToLogin() {
        runOnUiThread(() -> {
            Intent intent = new Intent(AppActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        });
    }

    private void goToMainActivity(){

            Intent intent = new Intent(AppActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

            return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }

        return false;
    }

    private void loadLocations() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (database.locationsDao().getSize() == 0) {
                List<String> locations = Arrays.asList(
                        "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan", "Bizerte", "Béja",
                        "Jendouba", "Kef", "Siliana", "Sousse", "Monastir", "Mahdia", "Sfax", "Kairouan",
                        "Kasserine", "Sidi Bouzid", "Gabès", "Medenine", "Tataouine", "Gafsa", "Tozeur", "Kebili"
                );

                for (String locationName : locations) {
                    Location location = new Location();
                    location.locationName = locationName;
                    database.locationsDao().insertLocation(location);
                }
            }
        });
    }



}
