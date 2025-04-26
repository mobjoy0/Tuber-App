package com.project.tuber_app.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tuber_app.R;
import com.project.tuber_app.entities.Ride;

import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private final List<Ride> rideList;
    private final OnRideClickListener onRideClickListener;

    // 1. Define the listener interface
    public interface OnRideClickListener {
        void onRideClick(Ride ride);
    }

    // 2. Constructor now accepts a click listener
    public RideAdapter(List<Ride> rideList, OnRideClickListener listener) {
        this.rideList = rideList;
        this.onRideClickListener = listener;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        try {
            Ride ride = rideList.get(position);

            holder.departureStation.setText(ride.getStartLocation());
            holder.arrivalStation.setText(ride.getEndLocation());
            holder.duration.setText(String.valueOf(ride.getEta()));
            holder.departureTime.setText(ride.getDepartureTime());
            holder.arrivalTime.setText(String.valueOf(ride.getDepartureTime()));
            holder.seatAvailability.setText(String.valueOf(ride.getAvailableSeats()));
            holder.price.setText(ride.getPrice() + "dt");

            // 3. Set up click event
            holder.layout.setOnClickListener(v -> {
                if (onRideClickListener != null) {
                    onRideClickListener.onRideClick(ride);
                }
            });

        } catch (Exception e) {
            Log.wtf("ee", "exception: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView departureTime, departureStation,
                arrivalTime, arrivalStation, duration,
                seatAvailability, price;
        ConstraintLayout layout;


        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            departureTime = itemView.findViewById(R.id.tvDepartureTime);
            departureStation = itemView.findViewById(R.id.tvDepartureLocation);
            arrivalTime = itemView.findViewById(R.id.tvDestinationTime);
            arrivalStation = itemView.findViewById(R.id.tvDestinationLocation);
            duration = itemView.findViewById(R.id.tvEta);
            seatAvailability = itemView.findViewById(R.id.tvSeats);
            price = itemView.findViewById(R.id.tvPrice);
            layout = itemView.findViewById(R.id.layoutView);
        }
    }
}
