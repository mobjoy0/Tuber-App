package com.project.Tuber_backend.repository;

import com.project.Tuber_backend.entity.rideEntities.Booking;
import com.project.Tuber_backend.entity.rideEntities.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Integer> {

    Booking findBookingById(int id);
    Booking findBookingByPassengerId(int passengerId);
    List<Booking> findByPassengerIdAndStatus(int passengerId, Booking.BookingStatus status);
    List<Booking> findBookingByRideIdAndStatus(int rideId, Booking.BookingStatus status);
    List<Booking> findBookingByPassengerIdAndStatus(int passengerId, Booking.BookingStatus status);
    Booking findBookingByRideIdAndPassengerId(int rideId, int passengerId);

    List<Booking> findBookingsByPassengerIdAndStatus(int passengerId, Booking.BookingStatus status);

}
