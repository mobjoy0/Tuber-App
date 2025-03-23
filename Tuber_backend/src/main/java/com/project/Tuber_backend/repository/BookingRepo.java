package com.project.Tuber_backend.repository;

import com.project.Tuber_backend.entity.rideEntities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Integer> {

    Booking findBookingById(int id);
    Optional<Booking> findBookingByPassengerId(int passengerId);
    List<Booking> findBookingByRideId(int rideId);
    List<Booking> findBookingByPassengerIdAndStatus(int passengerId, Booking.BookingStatus status);
    Booking findBookingByRideIdAndPassengerId(int rideId, int passengerId);

}
