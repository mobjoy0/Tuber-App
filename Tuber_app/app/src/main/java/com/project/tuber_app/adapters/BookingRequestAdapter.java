package com.project.tuber_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.tuber_app.R;
import com.project.tuber_app.entities.Booking;

import java.util.List;

public class BookingRequestAdapter extends RecyclerView.Adapter<BookingRequestAdapter.BookingViewHolder> {

    private List<Booking> bookings;
    private Context context;
    private OnBookingActionClick listener;

    // Only REJECT interface
    public interface OnBookingActionClick {
        void onReject(Booking booking);
    }

    public BookingRequestAdapter(Context context, List<Booking> bookings, OnBookingActionClick listener) {
        this.context = context;
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_request, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);

        holder.departureLocation.setText(booking.getRide().getStartLocation());
        holder.destinationLocation.setText(booking.getRide().getEndLocation());
        holder.seatsRequested.setText(String.valueOf(booking.getSeatsBooked()));
        holder.departureTime.setText(booking.getRide().getRideTime());
        holder.arrivalTime.setText(booking.getRide().getRideEndTime());

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(booking);
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {

        TextView departureLocation, destinationLocation, seatsRequested, departureTime, arrivalTime;
        Chip statusChip;
        FloatingActionButton btnReject;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            departureLocation = itemView.findViewById(R.id.departureLocation);
            destinationLocation = itemView.findViewById(R.id.destinationLocation);
            seatsRequested = itemView.findViewById(R.id.seatsRequested);
            departureTime = itemView.findViewById(R.id.departureTime);
            arrivalTime = itemView.findViewById(R.id.arrivalTime);
            statusChip = itemView.findViewById(R.id.statusChip);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
