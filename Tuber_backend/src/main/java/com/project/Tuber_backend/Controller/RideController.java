package com.project.Tuber_backend.Controller;


import com.project.Tuber_backend.entity.rideEntities.Ride;
import com.project.Tuber_backend.service.RideService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
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
}
