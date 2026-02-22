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

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerJwtSecurityIT extends AbstractIntegrationTest {
    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE payment_cards RESTART IDENTITY CASCADE");
        clearAllCaches();
    }

    private Long createTestUser() throws Exception {
        String keycloakId = UUID.randomUUID().toString();
        String userJson = String.format("""
                {
                  "keycloakId": "%s",
                  "name": "Alice",
                  "surname": "Smith",
                  "birthDate": "1985-05-15",
                  "email": "alice.smith@example.com"
                }
                """, keycloakId);

        String response = mockMvc.perform(post("/users")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
                        .contentType(JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return parseId(response);
    }

    @Test
    @DisplayName("Should not have access without token")
    void shouldReturn401_whenNoToken() throws Exception {
        Long userId = createTestUser();

        mockMvc.perform(get("/admin/users/" + userId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("User cannot access to admin api")
    void shouldReturn403_whenUserRole() throws Exception {
        Long userId = createTestUser();

        mockMvc.perform(get("/admin/users/" + userId)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("Should gain access with admin role")
    void shouldReturn200_whenAdminRole() throws Exception {
        Long userId = createTestUser();

        mockMvc.perform(get("/admin/users/" + userId)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON))
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    @DisplayName("Admin should activate user successfully")
    void shouldReturn200_whenAdminDeactivatesUser() throws Exception {
        Long userId = createTestUser();

        // deactivate the user
        mockMvc.perform(put("/admin/users/" + userId + "/deactivate")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // verify user is deactivated
        mockMvc.perform(get("/admin/users/" + userId)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Admin should delete user successfully")
    void shouldReturn200_whenAdminDeletesUser() throws Exception {
        Long userId = createTestUser();

        mockMvc.perform(delete("/admin/users/" + userId)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // verify user is deleted
        mockMvc.perform(get("/admin/users/" + userId)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
                        .contentType(JSON))
                .andExpect(status().isNotFound());
    }
}


