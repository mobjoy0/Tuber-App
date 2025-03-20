package com.project.Tuber_backend.repository;

import com.project.Tuber_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phone);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phone);

}