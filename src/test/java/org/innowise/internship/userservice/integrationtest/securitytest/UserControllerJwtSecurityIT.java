package org.innowise.internship.userservice.integrationtest.securitytest;

import org.innowise.internship.userservice.integrationtest.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerJwtSecurityIT extends AbstractIntegrationTest {
    private String testUserKeycloakId;
    private Long testUserId;
    private Long testUserCardId;

    private Long createTestUser() throws Exception {
        testUserKeycloakId = UUID.randomUUID().toString();
        String userJson = String.format("""
                {
                  "keycloakId": "%s",
                  "name": "Alice",
                  "surname": "Smith",
                  "birthDate": "1985-05-15",
                  "email": "alice.smith@example.com"
                }
                """, testUserKeycloakId);

        String response = mockMvc.perform(post("/users")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ))
                        .contentType(JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return parseId(response);
    }

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE payment_cards RESTART IDENTITY CASCADE");
        clearAllCaches();

        testUserId = createTestUser();

        String cardJson = String.format("""
                {
                  "userId": %d,
                  "number": "4111111111111111",
                  "holder": "ALICE SMITH",
                  "expirationDate": "%s"
                }
                """, testUserId, LocalDate.now().plusYears(1).format(DATE_FORMATTER));

        String cardResponse = mockMvc.perform(post("/users/{userId}/cards", testUserId)
                        .with(jwt().jwt(jwt -> jwt.subject(testUserKeycloakId))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(JSON)
                        .content(cardJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        testUserCardId = parseId(cardResponse);
    }

    @Test
    @DisplayName("Access without jwt is denied")
    void shouldReturn401_whenGetUserByIdWithoutToken() throws Exception {
        mockMvc.perform(get("/users/{id}", testUserId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Forbidden to access profile of another user")
    void shouldReturn403_whenGetOtherUserProfile() throws Exception {
        String otherUserKeycloakId = UUID.randomUUID().toString();

        mockMvc.perform(get("/users/{id}", testUserId)
                        .with(jwt().jwt(jwt -> jwt.subject(otherUserKeycloakId))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should access own profile successfully")
    void shouldReturn200_whenGetOwnUserProfile() throws Exception {
        mockMvc.perform(get("/users/{id}", testUserId)
                        .with(jwt().jwt(jwt -> jwt.subject(testUserKeycloakId))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId))
                .andExpect(jsonPath("$.email").value("alice.smith@example.com"));
    }

    @Test
    @DisplayName("Should create user profile based on jwt")
    void shouldCreateUserBasedOnJwt() throws Exception {
        String newKeycloakId = UUID.randomUUID().toString();
        String newUserJson = String.format("""
                {
                  "keycloakId": "%s",
                  "name": "Bob",
                  "surname": "Johnson",
                  "birthDate": "1990-08-20",
                  "email": "bob.johnson@example.com"
                }
                """, newKeycloakId);

        mockMvc.perform(post("/users")
                .with(jwt()
                        .jwt(jwt -> jwt
                                .subject(newKeycloakId)
                        )
                        .authorities(new SimpleGrantedAuthority("ROLE_USER")
                ))
                .contentType(JSON)
                .content(newUserJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("bob.johnson@example.com"));
    }

    @Test
    @DisplayName("Forbidden to update another user profile")
    void shouldReturn403_whenUpdateOtherUserProfile() throws Exception {
        String otherUserKeycloakId = UUID.randomUUID().toString();
        String updateJson = """
                {
                  "name": "UpdatedName",
                  "surname": "UpdatedSurname"
                }
                """;

        mockMvc.perform(put("/users/{id}", testUserId)
                        .with(jwt().jwt(jwt -> jwt.subject(otherUserKeycloakId))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(JSON)
                        .content(updateJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Forbidden to delete another user profile")
    void shouldReturn403_whenDeleteOtherUser() throws Exception {
        String otherUserKeycloakId = UUID.randomUUID().toString();

        mockMvc.perform(delete("/users/{id}", testUserId)
                        .with(jwt().jwt(jwt -> jwt.subject(otherUserKeycloakId))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Forbidden to access cards of another user")
    void shouldReturn403_whenGetOtherUserCards() throws Exception {
        String otherUserKeycloakId = UUID.randomUUID().toString();

        mockMvc.perform(get("/users/{userId}/cards", testUserId)
                        .with(jwt().jwt(jwt -> jwt.subject(otherUserKeycloakId))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should access own card successfully")
    void shouldReturn200_whenGetOwnCard() throws Exception {
        mockMvc.perform(get("/users/{userId}/cards/{cardId}", testUserId, testUserCardId)
                        .with(jwt().jwt(jwt -> jwt.subject(testUserKeycloakId))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserCardId));
    }

    @Test
    @DisplayName("Forbidden to create card for another user")
    void shouldReturn403_whenCreateCardForOtherUser() throws Exception {
        String otherUserKeycloakId = UUID.randomUUID().toString();
        String cardJson = String.format("""
                {
                  "userId": %d,
                  "number": "4111111111119999",
                  "holder": "VITTORIO RUI",
                  "expirationDate": "%s"
                }
                """, 999, LocalDate.now().plusYears(5).format(DATE_FORMATTER));

        mockMvc.perform(post("/users/{userId}/cards", testUserId)
                        .with(jwt().jwt(jwt -> jwt.subject(otherUserKeycloakId))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(JSON)
                        .content(cardJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Forbidden to delete a card of another user")
    void shouldReturn403_whenDeleteOtherUserCard() throws Exception {
        String otherUserKeycloakId = UUID.randomUUID().toString();

        mockMvc.perform(delete("/users/{userId}/cards/{cardId}", testUserId, testUserCardId)
                        .with(jwt().jwt(jwt -> jwt.subject(otherUserKeycloakId))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin cannot update user profile data")
    void shouldReturn403_whenAdminRole() throws Exception {
        String updateJson = """
                {
                  "name": "UpdatedName",
                  "surname": "UpdatedSurname"
                }
                """;

        mockMvc.perform(put("/users/" + testUserId)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
                .contentType(JSON)
                .content(updateJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }
}
