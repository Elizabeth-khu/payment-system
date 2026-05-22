package com.innowise.userservice.service;

import com.innowise.userservice.dto.UserCreateDto;
import com.innowise.userservice.dto.UserResponseDto;
import com.innowise.userservice.dto.UserUpdateDto;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public interface UserService {

    UserResponseDto createUser(UserCreateDto createDto);

    UserResponseDto getUserById(String userId);

    Page<UserResponseDto> getAllUsers(String name, String surname, Pageable pageable);

    UserResponseDto updateUser(String id, UserUpdateDto updateDto);

    void deactivateUser(String id);
}
