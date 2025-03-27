package com.project.Tuber_backend.Controller;


import com.project.Tuber_backend.entity.rideEntities.Ride;
import com.project.Tuber_backend.entity.userEntities.User;
import com.project.Tuber_backend.repository.RideRepo;
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
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;
    private final RideRepo rideRepo;
    private final JwtService jwtService;
    private final UserService userService;

    public RideController(RideService rideService, RideRepo rideRepo, UserService userService, JwtService jwtService) {
        this.rideService = rideService;
        this.rideRepo = rideRepo;
        this.userService = userService;
        this.jwtService = jwtService;
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
            List<Ride> anyride = rideRepo.searchAvailableRides(origin,
                    destination,
                    departureTime,
                    departureTime.plusHours(hoursOffset),
                    maxPrice);
            for(Ride ride : anyride){
                ride.getDriver().setPassword(null);
                ride.getDriver().setCin(null);
                ride.getDriver().setEmail(null);
            }
            return ResponseEntity.ok(anyride);
        } catch (Exception e) {
            System.out.println("erro:" + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }


    }
}
