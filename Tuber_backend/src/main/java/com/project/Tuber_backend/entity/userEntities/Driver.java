package com.project.Tuber_backend.entity.userEntities;

import com.project.Tuber_backend.repository.RideRepo;
import com.project.Tuber_backend.entity.rideEntities.Ride;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class Driver extends User{

    public static boolean canDriverCreateNewRide(int driverId, LocalDateTime departureTime, RideRepo rideRepo) {
        // Check if the driver already has an active ride (IN_PROGRESS)
        Optional<Ride> activeInProgressRide = rideRepo.findRidesByDriverIdAndStatus(driverId, Ride.RideStatus.IN_PROGRESS);
        Optional<Ride> activeScheduledRides = (rideRepo.findRidesByDriverIdAndStatus(driverId, Ride.RideStatus.SCHEDULED));
        return activeScheduledRides.isEmpty() && activeInProgressRide.isEmpty() && (departureTime.isAfter(LocalDateTime.now().plusHours(1)));
    }

    public static boolean isDriver(User user) {
        return user.getRole().equals(User.Role.DRIVER);
    }
}
