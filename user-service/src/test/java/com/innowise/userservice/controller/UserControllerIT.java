package com.innowise.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.IntegrationTestBase;
import com.innowise.userservice.dto.UserCreateDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
class UserControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setName("TestName");
        user.setSurname("TestSurname");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        savedUser = userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private RequestPostProcessor auth(String userId, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        ));
    }

    @Test
    void shouldCreateUserSuccessfully_WhenAdmin() throws Exception {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setEmail("admin-test-new@mail.com");
        createDto.setName("AdminTest");
        createDto.setSurname("AdminTest");
        createDto.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/v1/users")
                        .with(auth("admin-id", "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("admin-test-new@mail.com"));
    }

    @Test
    void shouldReturnUserById_WhenAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", savedUser.getId())
                        .with(auth("admin-id", "ROLE_ADMIN")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    void shouldReturnUserById_WhenUserRequestsOwnProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", savedUser.getId())
                        .with(auth(savedUser.getId(), "ROLE_USER")))
                .andExpect(status().isOk());
    }


    @Test
    void shouldReturnForbidden_WhenUserRequestsAnotherProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", savedUser.getId())
                        .with(auth("some-other-uuid", "ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnForbidden_WhenUserTriesToCreateUser() throws Exception {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setEmail("hacker@mail.com");
        createDto.setName("Hacker");
        createDto.setSurname("Hacker");
        createDto.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/v1/users")
                        .with(auth("some-user-uuid", "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorized_WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", savedUser.getId()))
                .andExpect(status().isUnauthorized());
    }
}