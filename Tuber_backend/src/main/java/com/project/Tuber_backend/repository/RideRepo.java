package com.project.Tuber_backend.repository;

import com.project.Tuber_backend.entity.rideEntities.Ride;
import com.project.Tuber_backend.entity.userEntities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RideRepo extends JpaRepository<Ride, Integer> {
    Optional<Ride> findRidesById(int id);
    List<Ride> findRidesByDriverId(int driverId);
    List<Ride> findRidesByStatusAndStartLocationAndEndLocation(Ride.RideStatus status, String startLocation, String endLocation);
    Optional<Ride> findRidesByDriverIdAndStatus(int driver, Ride.RideStatus status);



}
