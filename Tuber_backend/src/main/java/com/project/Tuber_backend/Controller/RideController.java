package com.project.Tuber_backend.Controller;


import com.project.Tuber_backend.apis.GoogleMapsApi;
import com.project.Tuber_backend.entity.rideEntities.Booking;
import com.project.Tuber_backend.entity.rideEntities.Ride;
import com.project.Tuber_backend.entity.userEntities.User;
import com.project.Tuber_backend.repository.RideRepo;
import com.project.Tuber_backend.service.BookingService;
import com.project.Tuber_backend.service.JwtService;
import com.project.Tuber_backend.service.RideService;
import com.project.Tuber_backend.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;
    private final RideRepo rideRepo;
    private final JwtService jwtService;
    private final UserService userService;
    private final BookingService bookingService;

    public RideController(RideService rideService, RideRepo rideRepo, UserService userService, JwtService jwtService, BookingService bookingService) {
        this.rideService = rideService;
        this.rideRepo = rideRepo;
        this.userService = userService;
        this.jwtService = jwtService;
        this.bookingService = bookingService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createRide(@RequestHeader("Authorization") String authHeader,
                                             @RequestBody Ride ride) {

        String email = jwtService.extractEmailFromToken(authHeader);
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (ride.getDriver() == null || !user.getId().equals(ride.getDriver().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only create rides as yourself");
        }

        try{
            rideService.createRide(ride);
            return ResponseEntity.ok("Ride created successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Ride>> searchRides(@RequestParam @NotBlank String origin,
                                                  @RequestParam @NotBlank String destination,
                                                  @RequestParam LocalDateTime departureTime,
                                                  @RequestParam(defaultValue = "24") @Positive int hoursOffset,
                                                  @RequestParam(required = false) @Positive BigDecimal maxPrice) {
        try {
            List<Ride> rides = rideService.getAvailableRides(origin, destination, departureTime, hoursOffset, maxPrice);
            return ResponseEntity.ok(rides);
        } catch (Exception e) {
            System.out.println("erro:" + e.getMessage());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }


    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Ride>> getRideRecommendations(@RequestHeader("Authorization") String authHeader) {
        // Extract user email from token
        String email = jwtService.extractEmailFromToken(authHeader);
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        List<Ride> rides = rideService.getRideRecommendations(user);

        return ResponseEntity.ok(rides);
    }





    @PutMapping("/change-status/{status}")
    public ResponseEntity<String> changeRideStatus(@RequestHeader("Authorization") String authHeader, @PathVariable String status){
        String email = jwtService.extractEmailFromToken(authHeader);
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Ride ride = rideService.getScheduledRideByDriverId(user.getId());
        rideService.changeRideStatus(ride.getId(), status);
        return ResponseEntity.ok("Ride "+status+ " successfully");
    }

    @GetMapping("/getRideByDriver")
    public ResponseEntity<Ride> getRideByDriver(@RequestHeader("Authorization") String authHeader){
        String email = jwtService.extractEmailFromToken(authHeader);
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Ride ride = rideService.getRideByDriverIdAndStatus(user.getId(), Ride.RideStatus.SCHEDULED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending rides found for this driver"));
        ride.getDriver().setPassword(null);

        return ResponseEntity.ok(ride);
    }
}
