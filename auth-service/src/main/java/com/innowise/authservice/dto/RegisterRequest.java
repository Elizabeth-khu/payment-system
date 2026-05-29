package com.innowise.authservice.dto;

import com.innowise.authservice.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String userId;
    private String login;
    private String password;
    private Role role;
}