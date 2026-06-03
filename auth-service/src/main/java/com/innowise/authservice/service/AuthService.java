package com.innowise.authservice.service;

import com.innowise.authservice.dto.AuthRequest;
import com.innowise.authservice.dto.AuthResponse;
import com.innowise.authservice.dto.RegisterRequest;
import com.innowise.authservice.dto.TokenValidationResponse;

public interface AuthService {

    void saveCredentials(RegisterRequest request);

    AuthResponse createToken(AuthRequest request);

    AuthResponse refreshToken(String refreshToken);

    TokenValidationResponse validateToken(String token);
}