package com.project.Tuber_backend.Controller;

import com.project.Tuber_backend.entity.User;
import com.project.Tuber_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}