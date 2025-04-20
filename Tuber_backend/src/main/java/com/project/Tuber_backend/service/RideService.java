package com.project.Tuber_backend.service;


import com.project.Tuber_backend.entity.rideEntities.Ride;
import com.project.Tuber_backend.entity.userEntities.Driver;
import com.project.Tuber_backend.repository.RideRepo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RideService {
    private final RideRepo rideRepo;
    private final RestTemplate restTemplate;

    public RideService(RideRepo rideRepo, RestTemplate restTemplate) {
        this.rideRepo = rideRepo;
        this.restTemplate = restTemplate;
    }

    public Ride createRide(Ride ride) {

        if (!Driver.canDriverCreateNewRide(ride.getDriver().getId(), ride.getDepartureTime(), rideRepo) || !Driver.isDriver(ride.getDriver())) {
            throw new RuntimeException("an Error gas occurred while trying to create the ride.");
        }
        //ride.setRouteAndDistanceAndETA(restTemplate);

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

    public Ride getRideById(int id){
        return rideRepo.findRidesById(id).orElseThrow(() -> new RuntimeException("Ride not found!"));
    }

    public List<Ride> getAvailableRides(String origin,
                                        String destination,
                                        LocalDateTime departureTime,
                                        int hoursOffset,
                                        BigDecimal maxPrice){
        List<Ride> rides = rideRepo.searchAvailableRides(origin,
                destination,
                departureTime,
                departureTime.plusHours(hoursOffset),
                maxPrice);
        for(Ride ride : rides){
            ride.getDriver().setPassword(null);
            ride.getDriver().setCin(null);
            ride.getDriver().setEmail(null);
        }
        return rides;
    }




}
