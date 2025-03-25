package com.project.Tuber_backend.Controller;

import com.project.Tuber_backend.entity.userEntities.LoginRequest;
import com.project.Tuber_backend.entity.userEntities.User;
import com.project.Tuber_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            System.out.println("USER:"+user.getEmail());
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok("User registered successfully! ID: " + savedUser.getId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody LoginRequest loginRequest) {
        try {

            System.out.println("USER:"+loginRequest.getEmail()+"pass "+loginRequest.getPassword());
            Optional<User> user = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().body(null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<User> getUserProfile(@PathVariable int id) {
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/profile/update/{id}")
    public ResponseEntity<User> updateUserProfile(@PathVariable int id, @RequestBody User updatedUserDetails) {
        Optional<User> userOpt = userService.getUserById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (updatedUserDetails.getEmail() != null) {
                user.setEmail(updatedUserDetails.getEmail());
            }
            if (updatedUserDetails.getUserImage() != null) {
                user.setUserImage(updatedUserDetails.getUserImage());
            }

            if (updatedUserDetails.getPhoneNumber() != null) {
                String phoneNumber = updatedUserDetails.getPhoneNumber();
                // Check if phone number is 10 digits long
                if (phoneNumber.length() != 10) {
                    return ResponseEntity.status(400).body(null);
                }
                // Check if the phone number contains only digits
                if (!phoneNumber.matches("\\d{10}")) {
                    return ResponseEntity.status(400).body(null);
                }
                // Update the phone number
                user.setPhoneNumber(phoneNumber);
            }


            // Save the updated user details
            userService.save(user);

            return ResponseEntity.ok(null);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/profile/change-password/{id}")
    public ResponseEntity<String> changePassword(@PathVariable int id, @RequestBody String newPassword) {
        Optional<User> userOpt = userService.getUserById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.changePassword(newPassword);
            userService.save(user);
            return ResponseEntity.ok("Password changed successfully!");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}