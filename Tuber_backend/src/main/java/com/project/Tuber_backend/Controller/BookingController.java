package com.project.Tuber_backend.Controller;


import com.project.Tuber_backend.entity.rideEntities.Booking;
import com.project.Tuber_backend.repository.BookingRepo;
import com.project.Tuber_backend.service.BookingService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepo bookingRepo;

    public BookingController(BookingService bookingService, BookingRepo bookingRepo) {
        this.bookingService = bookingService;
        this.bookingRepo = bookingRepo;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createBooking(@RequestBody Booking booking) {
        try{
            bookingService.createBooking(booking);
            return ResponseEntity.ok("Booking created successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/requests/pending")
    private ResponseEntity<List<Booking>> retrievePendingBookings(@RequestParam int rideId){
        try {
            List<Booking> pendingRequests = bookingRepo.findBookingByRideIdAndStatus(rideId, Booking.BookingStatus.PENDING);
            return ResponseEntity.ok(pendingRequests);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/requests/confirmed")
    private ResponseEntity<List<Booking>> retrieveConfirmedBookings(@RequestParam int rideId){
        try {
            List<Booking> pendingRequests = bookingRepo.findBookingByRideIdAndStatus(rideId, Booking.BookingStatus.CONFIRMED);
            return ResponseEntity.ok(pendingRequests);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/change-status")
    public ResponseEntity<String> cancelBooking(@RequestParam int bookingId, @RequestParam @NotBlank String bookingStatus){
        try {
            bookingService.changeBookingStatus(bookingId, bookingStatus);
            return ResponseEntity.ok("Booking cancelled successfully");
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
