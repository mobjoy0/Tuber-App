package com.project.tuber_app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.project.tuber_app.R;
import com.project.tuber_app.activites.CodeVerificationActivity;
import com.project.tuber_app.activites.EditProfileActivity;
import com.project.tuber_app.activites.LoginActivity;
import com.project.tuber_app.activites.ResetPasswordActivity;
import com.project.tuber_app.databases.Database;
import com.project.tuber_app.databases.UserEntity;

import java.util.Arrays;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    private ImageView ivProfileImage;
    private TextView tvUserName, tvUserEmail;
    private MaterialButton btnEditProfile, btnLogout;
    private SwitchMaterial switchNotifications, switchDarkMode;
    private LinearLayout layoutEditProfileInfo, changePassword, changeEmail, applyDriver;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views
        ivProfileImage = rootView.findViewById(R.id.ivProfileImage);
        tvUserName = rootView.findViewById(R.id.tvUserName);
        tvUserEmail = rootView.findViewById(R.id.tvUserEmail);
        btnEditProfile = rootView.findViewById(R.id.btnEditProfile);
        btnLogout = rootView.findViewById(R.id.btnLogout);
        switchNotifications = rootView.findViewById(R.id.switchNotifications);
        switchDarkMode = rootView.findViewById(R.id.switchDarkMode);
        layoutEditProfileInfo = rootView.findViewById(R.id.layoutEditProfileInfo);
        changePassword = rootView.findViewById(R.id.layoutChangePassword);
        applyDriver = rootView.findViewById(R.id.layoutApplyDriver);
        changeEmail = rootView.findViewById(R.id.layoutChangeEmail);
        // Set default values
        setUserInfo();
        setPreferences();

        // Edit profile button click listener
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
            Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show();
        });

        layoutEditProfileInfo.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
            Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show();
        });

        applyDriver.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
            Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show();
        });

        changeEmail.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                UserEntity userEntity = database.userDao().getUser();

                requireActivity().runOnUiThread(() -> {
                    Intent intent = new Intent(getActivity(), CodeVerificationActivity.class);
                    intent.putExtra("USER_EMAIL", userEntity.email);  // Pass the email
                    intent.putExtra("actionType", "RESET_EMAIL"); // Pass actionType
                    startActivity(intent);  // Start the activity
                });
            });
        });

        changePassword.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                // Fetch the user from the database in the background thread
                UserEntity userEntity = database.userDao().getUser();

                // Switch to the main UI thread to interact with UI elements
                requireActivity().runOnUiThread(() -> {
                    // Create the Intent to navigate to ResetPasswordActivity
                    Intent intent = new Intent(getActivity(), CodeVerificationActivity.class);
                    intent.putExtra("USER_EMAIL", userEntity.email);  // Pass the email
                    intent.putExtra("actionType", "RESET_PASSWORD"); // Pass actionType
                    startActivity(intent);  // Start the activity
                });
            });
        });


        // Logout button click listener
        btnLogout.setOnClickListener(v -> {
            // Handle logout logic (e.g., clear session or preferences, redirect to login)
            logout();
        });

        // Notifications switch listener
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save notification preference
            savePreference("notifications_enabled", isChecked);
        });

        // Dark mode switch listener
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save dark mode preference
            savePreference("dark_mode_enabled", isChecked);
        });

        return rootView;
    }

    // Method to set user info (profile image, name, email)
    private Database database;
    private void setUserInfo() {


        Executors.newSingleThreadExecutor().execute(() -> {
            database = Database.getInstance(requireContext());
            UserEntity userEntity = database.userDao().getUser();

            requireActivity().runOnUiThread(() -> {
                tvUserName.setText(userEntity.firstName + " " + userEntity.lastName);
                tvUserEmail.setText(userEntity.email);
                Log.e("ee", "prifle pic=" + Arrays.toString(userEntity.userImage));

                Glide.with(this)
                        .load(userEntity.userImage)
                        .placeholder(R.drawable.ic_person)
                        .into(ivProfileImage);

            });
        });
    }


    private void setPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Set notifications preference
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notificationsEnabled);

        // Set dark mode preference
        boolean darkModeEnabled = prefs.getBoolean("dark_mode_enabled", false);
        switchDarkMode.setChecked(darkModeEnabled);
    }

    // Method to save preference to SharedPreferences
    private void savePreference(String key, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    // Method to handle logout
    private void logout() {
        // Clear preferences or session data and redirect to login screen
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();


        // Redirect to login activity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUserInfo();
    }

}
