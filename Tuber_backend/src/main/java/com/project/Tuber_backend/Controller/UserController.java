package com.project.Tuber_backend.Controller;

import com.project.Tuber_backend.entity.userEntities.LoginRequest;
import com.project.Tuber_backend.entity.userEntities.User;
import com.project.Tuber_backend.service.JwtService;
import com.project.Tuber_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/profile/change-password")
    public ResponseEntity<String> changePassword(@RequestHeader("Authorization") String authHeader,
                                                 @RequestBody String newPassword) {

        String email = jwtService.extractEmailFromToken(authHeader);
        Optional<User> userOpt = userService.getUserByEmail(email);

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