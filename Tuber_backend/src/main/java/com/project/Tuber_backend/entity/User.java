package com.project.Tuber_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {

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

    @Lob
    @Column(name = "user_image")
    private byte[] userImage;

    public enum Gender {
        MALE, FEMALE
    }

    public enum Role {
        DRIVER, RIDER
    }



}