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
import java.time.ZoneId;

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

    /**
     * Convert LocalDateTime to Unix timestamp (seconds since epoch)
     * @param dateTime The LocalDateTime to convert
     * @return Unix timestamp as an integer
     */
    private int convertToUnixTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return (int) (System.currentTimeMillis() / 1000); // Current time if null
        }
        return (int) dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    /**
     * Set route details using Google Maps API
     * @param restTemplate RestTemplate for API calls
     * @return true if successful, false otherwise
     */
    public boolean setRouteAndDistanceAndETA(RestTemplate restTemplate) {
        try {
            System.out.println("Setting route details for ride: " + this.id);

            // Convert departure time to Unix timestamp
            int departureTimestamp = convertToUnixTimestamp(this.departureTime);
            System.out.println("Departure time: " + this.departureTime + " -> Unix timestamp: " + departureTimestamp);

            GoogleMapsApi googleMapsApi = new GoogleMapsApi(restTemplate);

            // Set ride parameters with proper timestamp and locations
            googleMapsApi.setRideParameters(
                    departureTimestamp,
                    this.startLocation,
                    this.endLocation
            );

            System.out.println("Fetching route details from Google Maps API...");
            boolean success = googleMapsApi.getRideDetails();

            if (success) {
                System.out.println("Successfully retrieved route details");
                this.polyline = googleMapsApi.getPolyline();
                this.distance = googleMapsApi.getDistanceKm();
                this.eta = googleMapsApi.getEtaMinutes();

                System.out.println("Distance: " + this.distance + " km");
                System.out.println("ETA: " + this.eta + " minutes");
                System.out.println("Polyline length: " + (this.polyline != null ? this.polyline.length() : 0) + " characters");
                return true;
            } else {
                System.out.println("Failed to get route details: " + googleMapsApi.getLastError());
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error setting route details: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return "Ride{" +
                "id=" + id +
                ", driver=" + (driver != null ? driver.getId() : "null") +
                ", startLocation='" + startLocation + '\'' +
                ", endLocation='" + endLocation + '\'' +
                ", departureTime=" + departureTime +
                ", totalSeats=" + totalSeats +
                ", availableSeats=" + availableSeats +
                ", price=" + price +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", eta=" + eta +
                ", polyline='" + (polyline != null ? "[polyline data]" : "null") + '\'' +
                ", distance=" + distance +
                '}';
    }
}