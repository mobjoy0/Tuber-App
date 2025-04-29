package com.project.Tuber_backend.Controller;


import com.project.Tuber_backend.entity.rideEntities.Booking;
import com.project.Tuber_backend.entity.userEntities.User;
import com.project.Tuber_backend.repository.BookingRepo;
import com.project.Tuber_backend.service.BookingService;
import com.project.Tuber_backend.service.JwtService;
import com.project.Tuber_backend.service.RideService;
import com.project.Tuber_backend.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepo bookingRepo;
    private final JwtService jwtService;
    private final UserService userService;
    private final RideService rideService;


    public BookingController(BookingService bookingService, BookingRepo bookingRepo, JwtService jwtService, UserService userService, RideService rideService) {
        this.bookingService = bookingService;
        this.bookingRepo = bookingRepo;
        this.userService = userService;
        this.jwtService = jwtService;
        this.rideService = rideService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createBooking(@RequestHeader("Authorization") String authHeader,
                                                @RequestBody Booking booking) {
        try{

            String email = jwtService.extractEmailFromToken(authHeader);
            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

            if (!user.getId().equals(booking.getPassenger().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only book rides yourself");
            }
            if (bookingService.canBookRide(booking)){
                System.out.println("Booking is already booked");
                bookingService.createBooking(booking);
                return ResponseEntity.ok("Booking created successfully!");
            }else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/requests/pending")
    private ResponseEntity<List<Booking>> retrievePendingBookings(@RequestParam int rideId){
        try {
            List<Booking> pendingRequests = bookingRepo.findBookingByRideIdAndStatus(rideId, Booking.BookingStatus.PENDING);
            System.out.println("pending= "+ pendingRequests.size());
            return ResponseEntity.ok(pendingRequests);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/requests/confirmed")
    private ResponseEntity<List<Booking>> retrieveConfirmedBookings(@RequestParam int rideId){
        try {
            List<Booking> pendingRequests = bookingRepo.findBookingByRideIdAndStatus(rideId, Booking.BookingStatus.CONFIRMED);
            System.out.println("comfired= "+ pendingRequests.size());
            return ResponseEntity.ok(pendingRequests);
        } catch (RuntimeException e) {
            System.out.println("error= "+ e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/update-status")
    public ResponseEntity<String> cancelBooking(@RequestHeader("Authorization") String authHeader,
                                                @RequestParam int bookingId,
                                                @RequestParam @NotBlank String bookingStatus){
        try {

            Booking booking = bookingService.getBookingById(bookingId);
            if (booking.getStatus() == Booking.BookingStatus.CONFIRMED && bookingStatus.equals("CANCELED")) {
                bookingService.cancelBooking(booking);
                return ResponseEntity.ok("Booking "+bookingStatus+ " successfully");

            } else if (booking.getStatus() == Booking.BookingStatus.PENDING && bookingStatus.equals("CANCELED")) {
                bookingService.changeBookingStatus(booking, bookingStatus);
                return ResponseEntity.ok("Booking "+bookingStatus+ " successfully");
            } else if (booking.getStatus() == Booking.BookingStatus.CANCELED && bookingStatus.equals("CANCELED")) {
                System.out.println("already canceled");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Ride already canceled");
            }


            String email = jwtService.extractEmailFromToken(authHeader);
            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));


            if (!user.getId().equals(booking.getRide().getDriver().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only book rides yourself");
            }
            if (bookingStatus.equals("CONFIRMED")){
                rideService.reserveSeats(booking.getRide().getId(), booking.getSeatsBooked());
            }
            bookingService.changeBookingStatus(booking, bookingStatus);
            return ResponseEntity.ok("Booking "+bookingStatus+ " successfully");
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-booking")
    public ResponseEntity<List<Booking>> getBooking(@RequestHeader("Authorization") String authHeader, @RequestParam String status){
        System.out.println("here status=" + status);
        String email = jwtService.extractEmailFromToken(authHeader);
        User user = userService.getExistingUserByEmail(email);
        System.out.println("user= "+ user);
        List<Booking> bookings = bookingRepo.findBookingByPassengerIdAndStatus(user.getId(), Booking.BookingStatus.valueOf(status));
        System.out.println("booking= "+ bookings.size());
        if (bookings.isEmpty()){
            return ResponseEntity.notFound().build();
        } else {

            return ResponseEntity.ok(bookings);
        }
    }
}
