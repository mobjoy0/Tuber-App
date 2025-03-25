package com.project.Tuber_backend.service;


import com.project.Tuber_backend.entity.rideEntities.Booking;
import com.project.Tuber_backend.repository.BookingRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepo bookingRepo;
    private final RideService rideService;

    public BookingService(BookingRepo bookingRepo, RideService rideService) {
        this.bookingRepo = bookingRepo;
        this.rideService = rideService;
    }

    public void createBooking(Booking booking) {

        if (!booking.getPassenger().canBookRide(booking,bookingRepo)){
            throw new RuntimeException("an Error gas occurred while trying to create the booking.");
        }
        rideService.reserveSeats(booking.getRide().getId(), booking.getSeatsBooked());

        bookingRepo.save(booking);
    }

    public void changeBookingStatus(int id, String status) {
        Booking booking = bookingRepo.findBookingById(id);
        if (booking != null) {
            if (status.equals("CONFIRMED")){

                List<Booking> pendingBookings = bookingRepo.findBookingByPassengerIdAndStatus(booking.getPassenger().getId(),
                        Booking.BookingStatus.PENDING);

                for (Booking pendingBooking : pendingBookings){
                    pendingBooking.setStatus(Booking.BookingStatus.CANCELED);
                    bookingRepo.save(pendingBooking);
                }
            }
            booking.setStatus(Booking.BookingStatus.valueOf(status));
            bookingRepo.save(booking);
        } else {
            throw new RuntimeException("Booking not found!");
        }
    }

    public void cancelBooking(int id) {
        Booking booking = bookingRepo.findBookingById(id);
        if (booking != null) {
            bookingRepo.delete(booking);
        } else {
            throw new RuntimeException("Booking not found!");
        }
    }

    public Booking getBookingById(int id) {
        return bookingRepo.findBookingById(id);
    }

}
