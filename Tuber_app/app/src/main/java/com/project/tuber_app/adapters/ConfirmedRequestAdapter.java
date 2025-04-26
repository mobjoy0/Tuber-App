package com.project.tuber_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tuber_app.R;
import com.project.tuber_app.entities.Booking;

import java.util.List;

public class ConfirmedRequestAdapter extends RecyclerView.Adapter<ConfirmedRequestAdapter.ViewHolder> {

    private final List<Booking> bookingList;

    public ConfirmedRequestAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_confirmed_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.passengerName.setText(booking.getPassenger().getFirstName() + " " + booking.getPassenger().getLastName());
        holder.seatsBooked.setText(String.valueOf(booking.getSeatsBooked()));
        holder.phoneNumber.setText(booking.getPassenger().getPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView passengerName, seatsBooked, phoneNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            passengerName = itemView.findViewById(R.id.passengerName);
            seatsBooked = itemView.findViewById(R.id.seatsChip);
            phoneNumber = itemView.findViewById(R.id.phoneNumber);
        }
    }
}
