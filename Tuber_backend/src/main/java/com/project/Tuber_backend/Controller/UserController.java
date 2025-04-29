package com.project.Tuber_backend.Controller;

import com.project.Tuber_backend.entity.userEntities.ResetRequest;
import com.project.Tuber_backend.entity.userEntities.User;
import com.project.Tuber_backend.service.JwtService;
import com.project.Tuber_backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String authHeader) {

        String email = jwtService.extractEmailFromToken(authHeader);
        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }




    @PutMapping("/profile/update")
    public ResponseEntity<String> updateUserProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody User updatedUserDetails) {

        String email = jwtService.extractEmailFromToken(authHeader);
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // 3. Update user info
        User user = userOpt.get();
        ResponseEntity<String> response = user.updateUserInfo(updatedUserDetails);

        if (response.getStatusCode().is2xxSuccessful()) {
            userService.save(user);
            return response;
        }
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }



    @PatchMapping("/reset-email")
    public ResponseEntity<String> changeEmail(@RequestHeader("Authorization") String authHeader,
                                                 @RequestBody ResetRequest request) {
        String email = jwtService.extractEmailFromToken(authHeader);
        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (request.isValidEmail()) {
                user.setEmail(request.getEmail());
                userService.save(user);

                return ResponseEntity.ok("Email changed successfully!");
            } else {
                return ResponseEntity.badRequest().body("Invalid email format.");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    @PostMapping("/profile/upload")
    public ResponseEntity<String> changeProfilePic(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("image") MultipartFile file) {

        String email = jwtService.extractEmailFromToken(authHeader);
        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            User user = userOpt.get();
            user.setUserImage(file.getBytes());
            userService.save(user);
            System.out.println("image uploaded");

            return ResponseEntity.ok(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload");
        }
    }

    @GetMapping("/profile/image")
    public ResponseEntity<byte[]> getProfilePic(@RequestHeader("Authorization") String authHeader) {
        String email = jwtService.extractEmailFromToken(authHeader);
        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isPresent() && userOpt.get().getUserImage() != null) {
            User user = userOpt.get();
            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg") // or detect dynamically if you want
                    .body(user.getUserImage());
        } else {
            return ResponseEntity.notFound().build();
        }
    }



}