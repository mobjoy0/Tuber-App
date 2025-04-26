package com.project.tuber_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tuber_app.R;
import com.project.tuber_app.adapters.RideAdapter;
import com.project.tuber_app.entities.Ride;

import java.util.List;


public class RideResultsFragment extends Fragment {

    private RecyclerView ridesRecyclerView;
    private RideAdapter rideAdapter;
    private String TAG = "ee";
    private List<Ride> rideList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ridesearch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ridesRecyclerView = view.findViewById(R.id.ridesRecyclerView);
        ridesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (getArguments() != null) {
            rideList = getArguments().getParcelableArrayList("ride_list");
        }

        if (rideList != null && !rideList.isEmpty()) {
            rideAdapter = new RideAdapter(rideList, ride -> {
                Bundle bundle = new Bundle();
                bundle.putParcelable("selected_ride", ride);

                RideDetailsFragment detailFragment = new RideDetailsFragment();
                detailFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout, detailFragment)
                        .addToBackStack(null)
                        .commit();
            });
            ridesRecyclerView.setAdapter(rideAdapter);
        } else {
            Toast.makeText(requireContext(), "No rides found", Toast.LENGTH_SHORT).show();
        }
    }

}
