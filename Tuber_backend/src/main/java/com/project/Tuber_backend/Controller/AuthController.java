package com.project.Tuber_backend.Controller;

import com.project.Tuber_backend.entity.userEntities.LoginRequest;
import com.project.Tuber_backend.entity.userEntities.LoginResponse;
import com.project.Tuber_backend.entity.userEntities.ResetRequest;
import com.project.Tuber_backend.entity.userEntities.User;
import com.project.Tuber_backend.repository.UserRepo;
import com.project.Tuber_backend.service.EmailService;
import com.project.Tuber_backend.service.JwtService;
import com.project.Tuber_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final UserRepo userRepo;
    private final EmailService emailService;


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        System.out.println("login:"+request.getEmail()+" "+request.getPassword());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        User user = userService.getExistingUserByEmail(request.getEmail());
        String jwt = jwtService.generateToken(userDetails);

        LoginResponse response = new LoginResponse(jwt, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok("User registered successfully! ID: " + savedUser.getId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<String> changePassword(@RequestBody ResetRequest request) {
        // Retrieve user by email
        Optional<User> userOpt = userService.getUserByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Validate the new password (length check, etc.)
            if (isValidPassword(request.getNewPassword())) {
                user.changePassword(request.getNewPassword());
                userService.save(user);

                return ResponseEntity.ok("Password changed successfully!");
            } else {
                return ResponseEntity.badRequest().body("Invalid password format.");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }


    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestParam String email) {
        System.out.println(email);
        Optional<User> optionalUser = userRepo.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("User not found ");
        }

        User user = optionalUser.get();

        String code = generateCode();
        user.setVerificationCode(code);
        userRepo.save(user);

        emailService.sendVerificationEmail(user.getEmail(), code);
        System.out.println("verification email sent to " + user.getEmail());

        return ResponseEntity.ok("Verification code sent to " + user.getEmail());
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestParam String email, @RequestParam String code) {
        Optional<User> optionalUser = userRepo.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = optionalUser.get();

        if (user.getVerificationCode() == null) {
            return ResponseEntity.status(400).body("No verification code sent. Please request a code first.");
        }

        if (!user.getVerificationCode().equals(code)) {
            return ResponseEntity.status(400).body("Invalid verification code");
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        userRepo.save(user);

        return ResponseEntity.ok("Email verified successfully!");
    }


    private String generateCode() {
        int code = new Random().nextInt(1_000_000); // 0 to 999999
        return String.format("%06d", code);
    }



}
