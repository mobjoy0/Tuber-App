package com.project.tuber_app.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tuber_app.R;
import com.project.tuber_app.entities.Booking;

import java.util.List;

public class PendingRequestAdapter extends RecyclerView.Adapter<PendingRequestAdapter.ViewHolder> {

    private final List<Booking> bookingList;
    private final OnBookingActionClick listener;

    public interface OnBookingActionClick {
        void onAccept(Booking booking);
        void onReject(Booking booking);
    }

    public PendingRequestAdapter(List<Booking> bookingList, OnBookingActionClick listener) {
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try{
            Booking booking = bookingList.get(position);
            holder.passengerName.setText(booking.getPassenger().getFirstName() + " " + booking.getPassenger().getLastName());
            holder.seatsBooked.setText(String.valueOf(booking.getSeatsBooked()));
            holder.phoneNumber.setText(booking.getPassenger().getPhoneNumber());

            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.rejectButton.setVisibility(View.VISIBLE);

            holder.acceptButton.setOnClickListener(v -> listener.onAccept(booking));
            holder.rejectButton.setOnClickListener(v -> listener.onReject(booking));
        } catch (Exception e) {
            Log.wtf("ee", "eerror pending: "+ e.getMessage());
        }


    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView passengerName, seatsBooked, phoneNumber;
        ImageButton acceptButton, rejectButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            passengerName = itemView.findViewById(R.id.passengerName);
            seatsBooked = itemView.findViewById(R.id.seatsCount);
            phoneNumber = itemView.findViewById(R.id.phoneNumber);
            acceptButton = itemView.findViewById(R.id.btnAccept);
            rejectButton = itemView.findViewById(R.id.btnReject);
        }
    }
}
