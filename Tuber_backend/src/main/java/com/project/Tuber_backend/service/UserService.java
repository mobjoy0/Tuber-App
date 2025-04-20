package com.project.Tuber_backend.service;

import com.project.Tuber_backend.entity.userEntities.User;
import com.project.Tuber_backend.repository.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User registerUser(User user) {
        if (userRepo.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        if (userRepo.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new RuntimeException("Phone number already in use!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setVerified(false);

        return userRepo.save(user);
    }

    public Optional<User> loginUser(String email, String password) {
        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println(user.getId());


            if (passwordEncoder.matches(password, user.getPassword())) {
                user.setPassword(null);
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }
    public User getExistingUserByEmail(String email) {
        return userRepo.getUserByEmail(email);
    }

    public Optional<User> getUserById(int id) {
        return userRepo.findById(id);
    }

    public User save(User user) {
        return userRepo.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}