package com.innowise.authservice.service;

import com.innowise.authservice.dto.AuthRequest;
import com.innowise.authservice.dto.AuthResponse;
import com.innowise.authservice.dto.RegisterRequest;
import com.innowise.authservice.dto.TokenValidationResponse;
import com.innowise.authservice.entity.Role;
import com.innowise.authservice.entity.UserCredential;
import com.innowise.authservice.exception.InvalidTokenException;
import com.innowise.authservice.exception.UserAlreadyExistsException;
import com.innowise.authservice.exception.UserNotFoundException;
import com.innowise.authservice.repository.UserCredentialRepository;
import com.innowise.authservice.security.JwtService;
import com.innowise.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserCredentialRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Save Credentials: Success")
    void saveCredentials_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUserId("user-123");
        request.setLogin("new_login");
        request.setPassword("pass");
        request.setRole(Role.ROLE_USER);

        when(repository.findByLogin(request.getLogin())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_pass");

        authService.saveCredentials(request);

        verify(repository, times(1)).save(any(UserCredential.class));
    }

    @Test
    @DisplayName("Save Credentials: Fail when login already exists")
    void saveCredentials_Fail_UserExists() {
        RegisterRequest request = new RegisterRequest();
        request.setLogin("existing_login");

        when(repository.findByLogin(request.getLogin())).thenReturn(Optional.of(new UserCredential()));

        assertThrows(UserAlreadyExistsException.class, () -> authService.saveCredentials(request));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Create Token: Success")
    void createToken_Success() {
        AuthRequest request = new AuthRequest();
        request.setLogin("test_login");
        request.setPassword("pass");

        UserCredential user = UserCredential.builder().login("test_login").build();

        when(repository.findByLogin(request.getLogin())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("access");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh");

        AuthResponse response = authService.createToken(request);

        assertNotNull(response);
        assertEquals("access", response.getAccessToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Create Token: Fail when user not found in DB")
    void createToken_Fail_UserNotFound() {
        AuthRequest request = new AuthRequest();
        request.setLogin("unknown_login");
        request.setPassword("pass");

        when(repository.findByLogin(request.getLogin())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.createToken(request));
    }

    @Test
    @DisplayName("Validate Token: Return valid response when token is valid")
    void validateToken_Success() {
        String token = "valid_token";
        String userId = "user-1";
        UserCredential user = new UserCredential();

        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(user));
        when(jwtService.isAccessTokenValid(token, user)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn("ROLE_USER");

        TokenValidationResponse response = authService.validateToken(token);

        assertTrue(response.isValid());
        assertEquals("user-1", response.getUserId());
        assertEquals("ROLE_USER", response.getRole());
    }

    @Test
    @DisplayName("Validate Token: Return invalid response when exception occurs")
    void validateToken_Exception() {
        String token = "invalid_token";

        when(jwtService.extractUserId(token)).thenThrow(new RuntimeException("Token expired"));

        TokenValidationResponse response = authService.validateToken(token);

        assertFalse(response.isValid());
    }

    @Test
    @DisplayName("Refresh Token: Success")
    void refreshToken_Success() {
        String refreshToken = "valid_refresh";
        String userId = "user-1";
        UserCredential user = new UserCredential();

        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(user));
        when(jwtService.isRefreshTokenValid(refreshToken, user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("new_access");

        AuthResponse response = authService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals("new_access", response.getAccessToken());
        assertEquals("valid_refresh", response.getRefreshToken());
    }

    @Test
    @DisplayName("Refresh Token: Fail when token is invalid or null userId")
    void refreshToken_Fail_Invalid() {
        String refreshToken = "invalid_refresh";
        when(jwtService.extractUserId(refreshToken)).thenReturn(null);

        assertThrows(InvalidTokenException.class, () -> authService.refreshToken(refreshToken));
    }
}