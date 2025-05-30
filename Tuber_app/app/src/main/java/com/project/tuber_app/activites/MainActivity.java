package com.project.tuber_app.activites;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.project.tuber_app.R;
import com.project.tuber_app.databinding.ActivityMainBinding;
import com.project.tuber_app.fragments.*;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Fragment currentFragment = null; // Track the currently displayed fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        // Load initial fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Set up bottom nav
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                if (!(currentFragment instanceof HomeFragment)) {
                    loadFragment(new HomeFragment());
                }
                return true;
            } else if (itemId == R.id.nav_track) {
                if (!(currentFragment instanceof BookingFragment)) {
                    loadFragment(new BookingFragment());
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                if (!(currentFragment instanceof SettingsFragment)) {
                    loadFragment(new SettingsFragment());
                }
                return true;
            } else {
                return false;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        currentFragment = fragment; // Update the current fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }
}
