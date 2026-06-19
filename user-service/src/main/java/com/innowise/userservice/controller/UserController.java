package com.innowise.userservice.controller;

import com.innowise.userservice.dto.UserCreateDto;
import com.innowise.userservice.dto.UserResponseDto;
import com.innowise.userservice.dto.UserUpdateDto;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto createDto) {
        log.info("REST request to create user: {}", createDto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(createDto));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @PageableDefault Pageable pageable) {
        log.info("REST request to get all users");
        return ResponseEntity.ok(userService.getAllUsers(name, surname, pageable));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #id == authentication.principal")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") String id) {
        log.info("REST request to get user by id: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #id == authentication.principal")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable("id") String id, @Valid @RequestBody UserUpdateDto updateDto) {
        log.info("REST request to update user with id: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, updateDto));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserResponseDto> activateUser(@PathVariable("id") String id) {
        log.info("REST request to activate user with id: {}", id);
        return ResponseEntity.ok(userService.activateUser(id));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable("id") String id) {
        log.info("REST request to deactivate user with id: {}", id);
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}