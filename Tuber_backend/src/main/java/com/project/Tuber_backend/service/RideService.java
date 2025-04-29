package com.project.Tuber_backend.service;


import com.project.Tuber_backend.entity.rideEntities.Booking;
import com.project.Tuber_backend.entity.rideEntities.Ride;
import com.project.Tuber_backend.entity.userEntities.Driver;
import com.project.Tuber_backend.entity.userEntities.User;
import com.project.Tuber_backend.repository.RideRepo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.awt.image.VolatileImage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RideService {
    private final RideRepo rideRepo;
    private final RestTemplate restTemplate;
    private final BookingService bookingService;

    public RideService(RideRepo rideRepo, RestTemplate restTemplate, @Lazy BookingService bookingService) {
        this.rideRepo = rideRepo;
        this.restTemplate = restTemplate;
        this.bookingService = bookingService;
    }

    public void createRide(Ride ride) {

        if (!Driver.canDriverCreateNewRide(ride.getDriver().getId(), ride.getDepartureTime(), rideRepo) || !Driver.isDriver(ride.getDriver())) {
            throw new RuntimeException("an Error gas occurred while trying to create the ride.");
        }
        ride.setRouteAndDistanceAndETA(restTemplate);

        rideRepo.save(ride);
    }

    @Transactional
    public void addCanceledSeats(int id, int seats) {
        Optional<Ride> rideOptional = rideRepo.findRidesById(id);
        if (rideOptional.isPresent()) {
            Ride ride = rideOptional.get();
            ride.setAvailableSeats(ride.getAvailableSeats() + seats);
            System.out.println("Available seats: " + ride.getAvailableSeats());
            rideRepo.save(ride);
        } else {
            throw new RuntimeException("Ride not found!");
        }
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

    public Ride getScheduledRideByDriverId(int driverId) {
        return rideRepo.findRideBydriverIdAndStatus(driverId, Ride.RideStatus.SCHEDULED)
                .orElseThrow(() -> new RuntimeException("No scheduled ride found for this driver!"));
    }

    public Ride findRideByDriverId(int driverId) {
        return rideRepo.findRidesByDriverIdAndStatus(driverId, Ride.RideStatus.SCHEDULED)
                .orElseThrow(() -> new RuntimeException("No ride found for this driver!"));
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


    public List<Ride> getRideRecommendations(User user) {
        // Step 1: Fetch user booking history
        List<Booking> bookingsMade = bookingService.getBookingHistory(user);

        List<Ride> availableRides = rideRepo.findUpcomingRides(); // 🛠️ Fetch available rides early

        if (bookingsMade.isEmpty()) {
            System.out.println("No bookings found!");
            // If no booking history, suggest rides starting from user's current location (city)
            return new ArrayList<>();
        }

        List<Ride> userPastRides = bookingsMade.stream()
                .map(Booking::getRide)
                .collect(Collectors.toList());

        // Step 2: Analyze behavior
        Map<String, Long> favoriteStartLocations = userPastRides.stream()
                .collect(Collectors.groupingBy(Ride::getStartLocation, Collectors.counting()));

        Map<String, Long> favoriteEndLocations = userPastRides.stream()
                .collect(Collectors.groupingBy(Ride::getEndLocation, Collectors.counting()));

        double averageDistance = userPastRides.stream()
                .filter(ride -> ride.getDistance() != null)
                .mapToInt(Ride::getDistance)
                .average()
                .orElse(0);

        List<Integer> pastHours = userPastRides.stream()
                .map(ride -> ride.getDepartureTime().getHour())
                .toList();

        int averageDepartureHour = (int) pastHours.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(12);

        // Step 3: Score each ride
        Map<Ride, Integer> rideScores = new HashMap<>();

        for (Ride ride : availableRides) {
            int score = 0;

            // Start location match
            score += favoriteStartLocations.getOrDefault(ride.getStartLocation(), 0L) * 3;

            // End location match
            score += favoriteEndLocations.getOrDefault(ride.getEndLocation(), 0L) * 3;

            // Departure time closeness
            if (Math.abs(ride.getDepartureTime().getHour() - averageDepartureHour) <= 2) {
                score += 2;
            }

            // Distance similarity
            if (ride.getDistance() != null && Math.abs(ride.getDistance() - averageDistance) <= 20) {
                score += 1;
            }

            // Boost if startLocation matches user's current city
            //if (ride.getStartLocation().equalsIgnoreCase(userLocation)) {score += 5;}
            if (score > 0){
                rideScores.put(ride, score);

            }
        }

        // Step 4: Sort rides by score (highest first)
        return rideScores.isEmpty()
                ? new ArrayList<>()  // Return empty list if no scores were found
                : rideScores.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())) // Sort in descending order
                .limit(4) // Limit to top 4 results
                .map(Map.Entry::getKey) // Extract the ride objects
                .collect(Collectors.toList());

    }





}
