package com.project.Tuber_backend.entity.rideEntities;

import com.project.Tuber_backend.apis.GoogleMapsApi;
import com.project.Tuber_backend.entity.userEntities.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id", referencedColumnName = "userID", nullable = false)
    private User driver;

    @Column(name = "start_location", nullable = false, length = 45)
    private String startLocation;

    @Column(name = "end_location", nullable = false, length = 45)
    private String endLocation;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RideStatus status = RideStatus.SCHEDULED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // New columns
    @Column(name = "ETA")
    private Integer eta; // Estimated Time of Arrival (in minutes)

    @Column(name = "polyline", columnDefinition = "TEXT")
    private String polyline; // Encoded polyline for the route

    @Column(name = "distance")
    private Integer distance; // Distance in kilometers

    public enum RideStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELED
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void setRouteAndDistanceAndETA(RestTemplate restTemplate) {
        System.out.println("Setting route, distance, and ETA for ride: " + this.id);
        GoogleMapsApi googleMapsApi = new GoogleMapsApi(10000, startLocation, endLocation , restTemplate);
        googleMapsApi.getRideDetails();
        this.polyline = googleMapsApi.getPolyline();
        this.distance = googleMapsApi.getDistance();
        this.eta = googleMapsApi.getETA();

    }
}
