package com.project.Tuber_backend.entity.userEntities;

import com.project.Tuber_backend.entity.rideEntities.Booking;
import com.project.Tuber_backend.repository.BookingRepo;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID")
    private Integer id;

    @Column(name = "first_name", length = 45)
    @NotNull(message = "First name is required")
    @Size(min = 2, max = 45, message = "First name must be between 2 and 45 characters")
    private String firstName;

    @Column(name = "last_name", length = 45)
    @NotNull(message = "Last name is required")
    @Size(min = 2, max = 45, message = "Last name must be between 2 and 45 characters")
    private String lastName;

    @Column(name = "email", unique = true, length = 45)
    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Column(name = "phone_number", unique = true, length = 45)
    @NotNull(message = "Phone number is required")
    @Size(min = 10, max = 45, message = "Phone number must be between 10 and 45 characters")
    private String phoneNumber;

    @Column(name = "CIN", length = 45, unique = true)
    @NotNull(message = "CIN is required")
    private String cin;

    @Column(name = "password", nullable = false, length = 255)
    @NotNull(message = "Password is required")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    @NotNull(message = "Gender is required")
    private Gender gender;

    @Column(name = "verified")
    private Boolean verified = false;

    @Column(name = "birth_date", nullable = false)
    @NotNull(message = "Birth date is required")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @NotNull(message = "Role is required")
    private Role role;

    @Column(name = "verification_code")
    private String verificationCode;

    @Lob
    @Column(name = "user_image")
    private byte[] userImage;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public enum Gender {
        MALE, FEMALE
    }

    public enum Role {
        DRIVER, RIDER
    }

    public void changePassword(String password){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        this.password = passwordEncoder.encode(password);
    }

    public boolean canBookRide(Booking booking, BookingRepo bookingRepo) {
        boolean hasConfirmedBooking = !bookingRepo.findBookingByPassengerIdAndStatus(this.id, Booking.BookingStatus.CONFIRMED).isEmpty();
        boolean alreadyBookedThisRide = bookingRepo.findBookingByRideIdAndPassengerId(booking.getRide().getId(), this.id) != null;
        int bookingCount = bookingRepo.findBookingByPassengerIdAndStatus(this.id, Booking.BookingStatus.PENDING).size();

        return !hasConfirmedBooking && !alreadyBookedThisRide && bookingCount < 3;
    }

    public ResponseEntity<String> updateUserInfo(User updatedUserDetails){


        if (updatedUserDetails.getPhoneNumber() != null) {
            String newPhoneNumber = updatedUserDetails.getPhoneNumber();

            if (newPhoneNumber.length() != 8) {
                return ResponseEntity.status(400).body("Phone number must be 10 digits");
            }
            // Check if the phone number contains only digits
            if (!newPhoneNumber.matches("\\d{10}")) {
                return ResponseEntity.status(400).body("Phone number cant contain characters");
            }
            phoneNumber = updatedUserDetails.getPhoneNumber();
        }

        if (updatedUserDetails.getFirstName() != null) {
            String newFirstName = updatedUserDetails.getFirstName();
            if (newFirstName.length() < 2 || newFirstName.length() > 45) {
                return ResponseEntity.status(400).body("First name must be between 2 and 45 characters");
            }
            firstName = updatedUserDetails.getFirstName();
        }

        if (updatedUserDetails.getLastName() != null) {
            String newLastName = updatedUserDetails.getLastName();
            if (newLastName.length() < 2 || newLastName.length() > 45) {
                return ResponseEntity.status(400).body("Last name must be between 2 and 45 characters");
            }
            lastName = updatedUserDetails.getLastName();
        }

        return ResponseEntity.ok().body("User updated successfully");
    }




}