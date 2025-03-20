package com.project.Tuber_backend.service;

import com.project.Tuber_backend.entity.User;
import com.project.Tuber_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;// Secure password hashing
    }

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new RuntimeException("Phone number already in use!");
        }

        // Hash the password before saving
        user.setPassword((user.getPassword()));

        // Set default verification status
        user.setVerified(false);

        return userRepository.save(user);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByPhoneNumber(String phone) {
        return userRepository.findByPhoneNumber(phone);
    }
}