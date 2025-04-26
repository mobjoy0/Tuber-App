package com.project.tuber_app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.project.tuber_app.R;
import com.project.tuber_app.entities.User;
import com.project.tuber_app.api.ApiClient;
import com.project.tuber_app.api.RideApi;
import com.project.tuber_app.databases.Database;
import com.project.tuber_app.databases.RideHistory;
import com.project.tuber_app.databases.UserDao;
import com.project.tuber_app.entities.Ride;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextInputEditText fromInput;
    private TextInputEditText toInput;
    private MaterialButton searchButton;
    private static final String TAG = "HomeFragment";
    private User user;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fromInput = view.findViewById(R.id.from_input);
        toInput = view.findViewById(R.id.to_input);
        searchButton = view.findViewById(R.id.search_button);


        Database db = Database.getInstance(getContext());
        UserDao userDao = db.userDao();


        searchButton.setOnClickListener(v -> {
            String from = fromInput.getText() != null ? fromInput.getText().toString().trim() : "";
            String to = toInput.getText() != null ? toInput.getText().toString().trim() : "";

            if (from.isEmpty() || to.isEmpty()) {
                Toast.makeText(getContext(), "Please fill both fields", Toast.LENGTH_SHORT).show();
            } else {
                searchRides(from, to);

            }
        });
    }



    private void searchRides(String origin, String destination) {
        Log.d(TAG, "Searching for rides from " + origin + " to " + destination);

        RideApi api = ApiClient.getRideApi(getContext());


        Call<List<Ride>> call = api.searchRides(origin, destination, "2025-04-01T15:20:00");

        call.enqueue(new Callback<List<Ride>>() {
            @Override
            public void onResponse(Call<List<Ride>> call, Response<List<Ride>> response) {
                Log.d(TAG, "API response received");

                if (response.isSuccessful() && response.body() != null) {
                    List<Ride> rides = response.body();
                    Log.d(TAG, "Found " + rides.size() + " rides");

                    if (rides.isEmpty()) {
                        Toast.makeText(getContext(), "No rides found.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Found " + rides.size() + " rides", Toast.LENGTH_SHORT).show();
                        navigateToRideResults(rides);

                    }
                } else {
                    Log.e(TAG, "Response unsuccessful: " + response.code());
                    Toast.makeText(getContext(), "Failed to load rides.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Ride>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void navigateToRideResults(List<Ride> rideList) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("ride_list", new ArrayList<>(rideList));

        RideResultsFragment fragment = new RideResultsFragment();
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void recommandedRides(View view, Database db){
        RecyclerView recyclerView = view.findViewById(R.id.recommandedRidesRecycleView);
        executorService.submit(() -> {
            List<RideHistory> rides = db.rideHistoryDao().getRideHistory(user.getId());


        });




    }


}
