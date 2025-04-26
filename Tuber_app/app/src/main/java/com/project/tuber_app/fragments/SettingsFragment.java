package com.project.tuber_app.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class SettingsFragment extends Fragment {

    private ImageView ivProfileImage;
    private TextView tvUserName, tvUserEmail;
    private MaterialButton btnEditProfile, btnLogout;
    private SwitchMaterial switchNotifications, switchDarkMode;

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

        // Set default values
        setUserInfo();
        setPreferences();

        // Edit profile button click listener
        btnEditProfile.setOnClickListener(v -> {
            // Navigate to EditProfileActivity (this should be implemented in your project)
            // Example: startActivity(new Intent(getActivity(), EditProfileActivity.class));
            Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show();
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
    private void setUserInfo() {
        // Example: Retrieve data from SharedPreferences or database
        String userName = "Wessim Benna"; // Replace with actual user data
        String userEmail = "john.doe@example.com"; // Replace with actual user data
        String userImageUrl = "https://example.com/path/to/profile/image.jpg"; // Replace with actual URL

        tvUserName.setText(userName);
        tvUserEmail.setText(userEmail);

        // Load profile image using Glide or any other image loading library
        Glide.with(this)
                .load(userImageUrl)
                .placeholder(R.drawable.ic_person) // Replace with your placeholder image
                .into(ivProfileImage);
    }

    // Method to set preferences (Notifications, Dark Mode)
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Redirect to login activity
        // Example: startActivity(new Intent(getActivity(), LoginActivity.class));
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}
