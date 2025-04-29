package com.project.Tuber_backend.service;


import com.project.Tuber_backend.entity.rideEntities.Booking;
import com.project.Tuber_backend.entity.rideEntities.Ride;
import com.project.Tuber_backend.entity.userEntities.User;
import com.project.Tuber_backend.repository.BookingRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        System.out.println(booking.getRide().getAvailableSeats());
        if (booking.getRide().getAvailableSeats() < booking.getSeatsBooked()){
            throw new RuntimeException("not enough seats.");
        }

        bookingRepo.save(booking);
    }

    public void changeBookingStatus(Booking booking, String status) {
        if (booking != null) {
            if (status.equals("CONFIRMED")){

                rideService.reserveSeats(booking.getId(), booking.getSeatsBooked());
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

    public void cancelBooking(Booking booking) {
        if (booking != null) {
            booking.setStatus(Booking.BookingStatus.CANCELED);
            rideService.addCanceledSeats(booking.getRide().getId(), booking.getSeatsBooked());
            bookingRepo.save(booking);
        } else {
            throw new RuntimeException("Booking not found!");
        }
    }

    public List<Booking> getBookingHistory(User user) {
        // Fetch all completed bookings
        List<Booking> completedBookings = bookingRepo.findBookingsByPassengerIdAndStatus(user.getId(), Booking.BookingStatus.COMPLETED);

        // Fetch all pending bookings
        List<Booking> pendingBookings = bookingRepo.findBookingsByPassengerIdAndStatus(user.getId(), Booking.BookingStatus.PENDING);

        // Combine both lists
        List<Booking> allBookings = new ArrayList<>();
        allBookings.addAll(completedBookings);
        allBookings.addAll(pendingBookings);

        return allBookings;
    }

    public boolean canBookRide(Booking booking) {
        return rideService.findRideByDriverId(booking.getRide().getDriver().getId()) == null;
    }




    public Booking getBookingById(int id) {
        return bookingRepo.findBookingById(id);
    }

}
