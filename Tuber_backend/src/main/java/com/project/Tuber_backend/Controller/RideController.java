package com.project.Tuber_backend.Controller;


import com.project.Tuber_backend.entity.rideEntities.Ride;
import com.project.Tuber_backend.repository.RideRepo;
import com.project.Tuber_backend.service.RideService;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;
    private final RideRepo rideRepo;

    public RideController(RideService rideService, RideRepo rideRepo) {
        this.rideService = rideService;
        this.rideRepo = rideRepo;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createRide(@RequestBody Ride ride) {
        try{
            rideService.createRide(ride);
            return ResponseEntity.ok("Ride created successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Ride>> searchRides(@RequestParam String origin,
                                                  @RequestParam String destination,
                                                  @RequestParam LocalDateTime departureTime,
                                                  @RequestParam(defaultValue = "24") int hoursOffset,
                                                  @RequestParam(required = false) BigDecimal maxPrice) {
        try {
            List<Ride> anyride = rideRepo.searchAvailableRides(origin,
                    destination,
                    departureTime,
                    departureTime.plusHours(hoursOffset),
                    maxPrice);

            return ResponseEntity.ok(anyride);
        } catch (Exception e) {
            System.out.println("erro:" + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }


    }
}
