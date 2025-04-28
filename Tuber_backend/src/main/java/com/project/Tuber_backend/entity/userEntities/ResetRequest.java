package com.project.Tuber_backend.entity.userEntities;

import java.util.regex.Pattern;

public class ResetRequest {
    private String email;
    private String newPassword;

    // Regular expression for basic email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public boolean isValidEmail() {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
