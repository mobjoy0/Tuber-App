package com.project.tuber_app.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.project.tuber_app.R;
import com.project.tuber_app.activites.EditProfileActivity;
import com.project.tuber_app.databases.UserEntity;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        super(R.layout.activity_profile); // Replace with your actual layout XML file
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get stored user ID (should be saved after login)
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        int userId = prefs.getInt("logged_in_user_id", 1);



    }
}
