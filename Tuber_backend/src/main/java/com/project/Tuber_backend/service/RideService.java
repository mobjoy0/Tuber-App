package com.project.Tuber_backend.service;


import com.project.Tuber_backend.entity.rideEntities.Ride;
import com.project.Tuber_backend.entity.userEntities.Driver;
import com.project.Tuber_backend.repository.RideRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RideService {
    private final RideRepo rideRepo;

    public RideService(RideRepo rideRepo) {
        this.rideRepo = rideRepo;
    }

    public Ride createRide(Ride ride) {

        if (!Driver.canDriverCreateNewRide(ride.getDriver().getId(), rideRepo) || !Driver.isDriver(ride.getDriver())) {
            throw new RuntimeException("an Error gas occurred while trying to create the ride.");
        }

        return rideRepo.save(ride);
    }

    @Transactional
    public void reserveSeats(int id, int seats) {
        Optional<Ride> rideOptional = rideRepo.findRidesById(id);
        if (rideOptional.isPresent()) {
            Ride ride = rideOptional.get();
            if (ride.getAvailableSeats() >= seats) {
                ride.setAvailableSeats(ride.getAvailableSeats() - seats);
                System.out.println("Available seats: " + ride.getAvailableSeats());
                rideRepo.save(ride);
            } else {
                throw new RuntimeException("Not enough seats available!");
            }
        } else {
            throw new RuntimeException("Ride not found!");
        }
    }

    @Transactional
    public void changeRideStatus(int id, String status) {
        Optional<Ride> rideOptional = rideRepo.findRidesById(id);
        if (rideOptional.isPresent()) {
            Ride ride = rideOptional.get();
            ride.setStatus(Ride.RideStatus.valueOf(status));
            rideRepo.save(ride);
        } else {
            throw new RuntimeException("Ride not found!");
        }
    }

    public List<Ride> getAllRides() {
        return rideRepo.findAll();
    }

    @Transactional
    public Ride updateRide(Ride ride) {
        return rideRepo.save(ride);
    }

    public Optional<Ride> getRideById(int id) {
        return rideRepo.findRidesById(id);
    }

    public Optional<Ride> getRideByDriverIdAndStatus(int driverId, Ride.RideStatus status) {
        return rideRepo.findRidesByDriverIdAndStatus(driverId, status);
    }

    public List<Ride> getRidesByDriverId(int driverId) {
        return rideRepo.findRidesByDriverId(driverId);
    }

    public List<Ride> findRidesByStatusAndStartLocationAndEndLocation(Ride.RideStatus status, String startLocation, String endLocation) {
        List<Ride> rides = rideRepo.findRidesByStatusAndStartLocationAndEndLocation(status, startLocation, endLocation);
        if (rides.isEmpty()){
            throw new RuntimeException("No rides found with the given parameters!");
        }else {
            return rides;
        }
    }




}
