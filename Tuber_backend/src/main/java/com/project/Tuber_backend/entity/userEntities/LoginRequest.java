package com.project.Tuber_backend.entity.userEntities;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    public String email;
    public String password;
}
