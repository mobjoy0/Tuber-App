package com.project.Tuber_backend.repository;

import com.project.Tuber_backend.entity.rideEntities.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RideRepo extends JpaRepository<Ride, Integer> {
    Optional<Ride> findRidesById(int id);
    List<Ride> findRidesByDriverId(int driverId);
    List<Ride> findRidesByStatusAndStartLocationAndEndLocation(Ride.RideStatus status,
                                                               String startLocation,
                                                               String endLocation);

    Optional<Ride> findRidesByDriverIdAndStatus(int driver, Ride.RideStatus status);

    Optional<Ride> findRideBydriverIdAndStatus(int driverId, Ride.RideStatus status);

    @Query("SELECT r FROM Ride r WHERE r.departureTime > CURRENT_TIMESTAMP AND r.status = 'SCHEDULED'")
    List<Ride> findUpcomingRides();


    @Query("SELECT r FROM Ride r WHERE "
            + "r.status = 'SCHEDULED' AND "
            + "r.availableSeats > 0 AND "
            + "(LOWER(r.startLocation) LIKE LOWER(CONCAT('%', :startLocation, '%'))) AND "
            + "(LOWER(r.endLocation) LIKE LOWER(CONCAT('%', :endLocation, '%'))) AND "
            + "r.departureTime BETWEEN :minDepartureTime AND :maxDepartureTime "
            + "AND (:maxPrice IS NULL OR r.price <= :maxPrice)" +
            " ORDER BY r.departureTime ASC")
    List<Ride> searchAvailableRides(
            @Param("startLocation") String startLocation,
            @Param("endLocation") String endLocation,
            @Param("minDepartureTime") LocalDateTime minDepartureTime,
            @Param("maxDepartureTime") LocalDateTime maxDepartureTime,
            @Param("maxPrice") BigDecimal maxPrice
    );


    @Query("SELECT r FROM Ride r WHERE r.status = 'SCHEDULED'")
    List<Ride> findAnyScheduledRides();





}
