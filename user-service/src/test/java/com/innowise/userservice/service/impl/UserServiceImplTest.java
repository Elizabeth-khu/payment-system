package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.UserResponseDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        String userId = "auth0|123";

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName("Liza");

        UserResponseDto mockDto = new UserResponseDto();
        mockDto.setId(userId);
        mockDto.setName("Liza");


        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toResponseDto(mockUser)).thenReturn(mockDto);


        UserResponseDto result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals("Liza", result.getName());
        assertEquals(userId, result.getId());

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toResponseDto(mockUser);
    }

    @Test
    void getUserById_ShouldReturnException_WhenUserDoesNotExist() {
        String wrongId = "auth0|999";

        when(userRepository.findById(wrongId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(wrongId);
        });

        verify(userMapper, never()).toResponseDto(any());
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {
        com.innowise.userservice.dto.UserCreateDto createDto = new com.innowise.userservice.dto.UserCreateDto();
        createDto.setEmail("test@example.com");

        User mappedUser = new User();
        mappedUser.setEmail("test@example.com");

        User savedUser = new User();
        savedUser.setId("auth0|new");
        savedUser.setEmail("test@example.com");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId("auth0|new");
        responseDto.setEmail("test@example.com");

        when(userMapper.toEntity(createDto)).thenReturn(mappedUser);
        when(userRepository.save(mappedUser)).thenReturn(savedUser);
        when(userMapper.toResponseDto(savedUser)).thenReturn(responseDto);

        UserResponseDto result = userService.createUser(createDto);

        assertNotNull(result);
        assertEquals("auth0|new", result.getId());
        verify(userRepository, times(1)).save(mappedUser);
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser() {
        String userId = "auth0|123";
        com.innowise.userservice.dto.UserUpdateDto updateDto = new com.innowise.userservice.dto.UserUpdateDto();
        updateDto.setName("NewName");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("OldName");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("NewName");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(userId);
        responseDto.setName("NewName");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userMapper).updateUserFromDto(updateDto, existingUser);
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.toResponseDto(updatedUser)).thenReturn(responseDto);

        UserResponseDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals("NewName", result.getName());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void deactivateUser_ShouldSetActiveToFalse() {
        String userId = "auth0|123";
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.deactivateUser(userId);

        assertEquals(false, existingUser.isActive());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void getAllUsers_ShouldReturnPaginatedUsers() {

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        User user = new User();
        user.setName("Ivan");
        org.springframework.data.domain.Page<User> userPage = new org.springframework.data.domain.PageImpl<>(java.util.List.of(user));

        UserResponseDto dto = new UserResponseDto();
        dto.setName("Ivan");

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(userPage);
        when(userMapper.toResponseDto(user)).thenReturn(dto);

        org.springframework.data.domain.Page<UserResponseDto> result = userService.getAllUsers("Ivan", null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Ivan", result.getContent().get(0).getName());
        verify(userRepository, times(1)).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }
}