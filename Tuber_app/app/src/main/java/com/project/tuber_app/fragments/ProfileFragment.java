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
import com.project.tuber_app.viewmodels.UserViewModel;

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

        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getUser(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                ((TextView) view.findViewById(R.id.tvFullName)).setText(user.firstName + " " + user.lastName);
                ((TextView) view.findViewById(R.id.value_email)).setText(user.email);
                ((TextView) view.findViewById(R.id.value_phone)).setText(user.phoneNumber);
                ((TextView) view.findViewById(R.id.value_cin)).setText(user.cin);
                ((TextView) view.findViewById(R.id.value_birth_date)).setText(user.birthDate.toString());
                ((TextView) view.findViewById(R.id.value_role)).setText(user.role.name());
            }
            else{
                Toast.makeText(getContext(), "User not found or empty", Toast.LENGTH_SHORT).show();
                ((TextView) view.findViewById(R.id.tvFullName)).setText("Unknown User");
                ((TextView) view.findViewById(R.id.value_email)).setText("-");
                ((TextView) view.findViewById(R.id.value_phone)).setText("-");
                ((TextView) view.findViewById(R.id.value_cin)).setText("-");
                ((TextView) view.findViewById(R.id.value_birth_date)).setText("-");
                ((TextView) view.findViewById(R.id.value_role)).setText("-");

            }
        });
        Button editButton = view.findViewById(R.id.btnEditProfile);
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

    }
}
