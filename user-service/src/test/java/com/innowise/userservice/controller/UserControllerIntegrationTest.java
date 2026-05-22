package com.innowise.userservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldCreateUserAndReturn201() throws Exception {
        String newUserJson = """
                {
                    "id": "123e4567-e89b-12d3-a456-426614174000",
                    "username": "elizabeth",
                    "name": "Lisa",
                    "surname": "Simpson",
                    "email": "elizabeth@example.com",
                    "password": "supersecretpassword",
                    "birthDate": "1990-05-15"
                }
                """;


        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("elizabeth@example.com"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void shouldGetUserByIdAndReturn200() throws Exception {
        String uniqueUserId = "999e4567-e89b-12d3-a456-426614174999";
        String newUserJson = """
                {
                    "id": "%s",
                    "username": "redis_tester",
                    "name": "John",
                    "surname": "Doe",
                    "email": "john.doe@example.com",
                    "password": "supersecretpassword",
                    "birthDate": "1995-10-10"
                }
                """.formatted(uniqueUserId);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserJson))
                .andExpect(status().isCreated());

        System.out.println("====== FIRST REQUEST (Expected to hit Postgres) ======");
        mockMvc.perform(get("/api/v1/users/" + uniqueUserId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.name").value("John"));

        System.out.println("====== SECOND REQUEST (Expected to hit Redis) ======");
        mockMvc.perform(get("/api/v1/users/" + uniqueUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    public void shouldUpdateUserAndReturn200() throws Exception {
        String uniqueUserId = "888e4567-e89b-12d3-a456-426614174888";
        String initialUserJson = """
                {
                    "id": "%s",
                    "username": "update_tester",
                    "name": "OldName",
                    "surname": "OldSurname",
                    "email": "old.email@example.com",
                    "password": "supersecretpassword",
                    "birthDate": "1990-01-01"
                }
                """.formatted(uniqueUserId);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(initialUserJson))
                .andExpect(status().isCreated());

        String updatedUserJson = """
                {
                    "name": "NewName",
                    "surname": "NewSurname"
                }
                """;

        mockMvc.perform(put("/api/v1/users/" + uniqueUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedUserJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"))
                .andExpect(jsonPath("$.surname").value("NewSurname"));
    }

    @Test
    public void shouldDeactivateUserAndReturn204() throws Exception {
        String uniqueUserId = "777e4567-e89b-12d3-a456-426614174777";
        String userJson = """
                {
                    "id": "%s",
                    "username": "delete_tester",
                    "name": "Mark",
                    "surname": "Twain",
                    "email": "mark.twain@example.com",
                    "password": "supersecretpassword",
                    "birthDate": "1835-11-30"
                }
                """.formatted(uniqueUserId);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/users/" + uniqueUserId))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldReturn404WhenUserNotFound() throws Exception {
        String nonExistentUserId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(get("/api/v1/users/" + nonExistentUserId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn400WhenEmailIsInvalidOnCreate() throws Exception {
        String invalidUserJson = """
                {
                    "id": "555e4567-e89b-12d3-a456-426614174555",
                    "username": "bad_email_tester",
                    "name": "Bob",
                    "surname": "Builder",
                    "email": "invalid-email-format",
                    "password": "supersecretpassword",
                    "birthDate": "1990-01-01"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldFilterUsersByNameWithPagination() throws Exception {

        String user1 = """
                {
                    "id": "a1111111-e89b-12d3-a456-426614174000",
                    "username": "alex_s",
                    "name": "Alex",
                    "surname": "Smith",
                    "email": "alex.smith@example.com",
                    "password": "password123",
                    "birthDate": "1990-01-01"
                }""";
        String user2 = """
                {
                    "id": "b2222222-e89b-12d3-a456-426614174000",
                    "username": "alex_j",
                    "name": "Alex",
                    "surname": "Johnson",
                    "email": "alex.johnson@example.com",
                    "password": "password123",
                    "birthDate": "1992-02-02"
                }""";

        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(user1)).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(user2)).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users?name=Alex&page=0&size=1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Alex"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }
}