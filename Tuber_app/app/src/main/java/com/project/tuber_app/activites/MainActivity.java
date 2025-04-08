package com.project.tuber_app.activites;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.tuber_app.R;
import com.project.tuber_app.adapters.ViewPagerAdapter;
import com.project.tuber_app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private int currentFrag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }
        overridePendingTransition(0, 0);

        // Setup ViewPager2 with adapter
        setupViewPager();

        // Setup BottomNavigationView listener
        setupBottomNavigation();

        // Add ViewPager page change callback
        binding.frameLayout.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentFrag = position;
                updateBottomNavigationSelection(position);
            }
        });

        // Restore selected fragment on configuration change
        if (savedInstanceState != null) {
            currentFrag = savedInstanceState.getInt("currentFrag", 0);
            binding.frameLayout.setCurrentItem(currentFrag);
        }
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.frameLayout.setAdapter(adapter);

        // Optional: Disable swipe if you only want navigation via bottom nav
        // binding.frameLayout.setUserInputEnabled(false);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                binding.frameLayout.setCurrentItem(0);
                return true;
            } else if (item.getItemId() == R.id.nav_track) {
                binding.frameLayout.setCurrentItem(1);
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                binding.frameLayout.setCurrentItem(2);
                return true;
            }
            return false;
        });
    }

    private void updateBottomNavigationSelection(int position) {
        binding.bottomNavigationView.getMenu().getItem(position).setChecked(true);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentFrag", currentFrag);
    }
}