package org.innowise.internship.userservice.integrationtest.controllertest;

import org.innowise.internship.userservice.controller.securitycontext.CurrentUserProvider;
import org.innowise.internship.userservice.integrationtest.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class AdminControllerIT extends AbstractIntegrationTest {
    @MockBean
    private CurrentUserProvider currentUserProvider;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE payment_cards RESTART IDENTITY CASCADE");
        clearAllCaches();
        //when(userSecurity.isOwner(anyLong())).thenReturn(true);
    }

    // methods for creating default user and card
    private Long createTestUser() throws Exception {
        String keycloakId = UUID.randomUUID().toString();
        String userJson = String.format("""
                {
                  "keycloakId": "%s",
                  "name": "John",
                  "surname": "Doe",
                  "birthDate": "1990-01-01",
                  "email": "john.doe@example.com"
                }
                """, keycloakId);

        String response = mockMvc.perform(post("/users")
                        .contentType(JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return parseId(response);
    }

    private Long createTestPaymentCard(Long userId) throws Exception {
        String cardJson = String.format("""
                {
                  "userId": %d,
                  "number": "4111111111111111",
                  "holder": "JOHN DOE",
                  "expirationDate": "%s"
                }
                """, userId, LocalDate.now().plusYears(1).format(DATE_FORMATTER));

        when(currentUserProvider.getCurrentInternalId()).thenReturn(userId);
        String response = mockMvc.perform(post("/users/my-cards")
                        .contentType(JSON)
                        .content(cardJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return parseId(response);
    }

    @Test
    @DisplayName("Admin should get all users successfully")
    void adminGetAllUsers_shouldGetAllSuccessfully() throws Exception {
        // create multiple users
        for (int i = 0; i < 3; i++) {
            String keycloakId = UUID.randomUUID().toString();
            String userJson = String.format("""
                {
                  "keycloakId": "%s",
                  "name": "User%d",
                  "surname": "Test",
                  "birthDate": "1990-01-01",
                  "email": "user%d@example.com"
                }
                """, keycloakId, i, i);

            mockMvc.perform(post("/users")
                            .contentType(JSON)
                            .content(userJson))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/admin/users")
                        .contentType(JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("Admin should filter users successfully")
    void adminGetAllUsers_shouldFilterSuccessfully() throws Exception {
        createTestUser();

        // Create another user with different name
        String keycloakId = UUID.randomUUID().toString();
        String secondUserJson = String.format("""
                {
                  "keycloakId": "%s",
                  "name": "Alice",
                  "surname": "Smith",
                  "birthDate": "1995-05-15",
                  "email": "alice.smith@example.com"
                }
                """, keycloakId);

        mockMvc.perform(post("/users")
                        .contentType(JSON)
                        .content(secondUserJson))
                .andExpect(status().isCreated());

        // filter by name
        mockMvc.perform(get("/admin/users")
                        .contentType(JSON)
                        .param("name", "Alice")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Alice"));
    }

    @Test
    @DisplayName("Admin should get user by ID successfully")
    void adminGetUserById_shouldGetSuccessfully() throws Exception {
        Long userId = createTestUser();

        mockMvc.perform(get("/admin/users/" + userId)
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("Admin should activate user successfully")
    void adminActivateUser_shouldActivateSuccessfully() throws Exception {
        Long userId = createTestUser();

        // deactivate the user
        mockMvc.perform(put("/admin/users/" + userId + "/deactivate")
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // verify user is inactive
        mockMvc.perform(get("/admin/users/" + userId)
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // activate via admin
        mockMvc.perform(put("/admin/users/" + userId + "/activate")
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // verify user is active
        mockMvc.perform(get("/admin/users/" + userId)
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Admin should deactivate user successfully")
    void adminDeactivateUser_shouldDeactivateSuccessfully() throws Exception {
        Long userId = createTestUser();

        mockMvc.perform(put("/admin/users/" + userId + "/deactivate")
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // verify user is inactive
        mockMvc.perform(get("/admin/users/" + userId)
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Admin should delete user successfully")
    void adminDeleteUser_shouldDeleteSuccessfully() throws Exception {
        Long userId = createTestUser();

        mockMvc.perform(delete("/admin/users/" + userId)
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // verify user is deleted
        mockMvc.perform(get("/admin/users/" + userId)
                        .contentType(JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Admin should get user cards successfully")
    void adminGetUserCards_shouldGetAllSuccessfully() throws Exception {
        Long userId = createTestUser();
        createTestPaymentCard(userId);

        mockMvc.perform(get("/admin/users/" + userId + "/cards")
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].maskedNumber").value("************1111"));
    }

    @Test
    @DisplayName("Admin should activate payment card successfully")
    void adminActivatePaymentCard_shouldActivateSuccessfully() throws Exception {
        Long userId = createTestUser();
        Long cardId = createTestPaymentCard(userId);

        // deactivate the card
        mockMvc.perform(put("/admin/users/" + userId + "/cards/" + cardId + "/deactivate")
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // activate via admin
        mockMvc.perform(put("/admin/users/" + userId + "/cards/" + cardId + "/activate")
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // verify card is active
        when(currentUserProvider.getCurrentInternalId()).thenReturn(userId);
        mockMvc.perform(get("/users/my-cards/" + cardId)
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Admin should deactivate payment card successfully")
    void adminDeactivatePaymentCard_shouldDeactivateSuccessfully() throws Exception {
        Long userId = createTestUser();
        Long cardId = createTestPaymentCard(userId);

        mockMvc.perform(put("/admin/users/" + userId + "/cards/" + cardId + "/deactivate")
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // verify card is inactive
        when(currentUserProvider.getCurrentInternalId()).thenReturn(userId);
        mockMvc.perform(get("/users/my-cards/" + cardId)
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Admin should handle pagination successfully")
    void adminGetAllUsers_shouldHandlePaginationSuccessfully() throws Exception {
        // create 15 users
        for (int i = 0; i < 15; i++) {
            String keycloakId = UUID.randomUUID().toString();
            String userJson = String.format("""
                {
                  "keycloakId": "%s",
                  "name": "User%d",
                  "surname": "Test",
                  "birthDate": "1990-01-01",
                  "email": "user%d@example.com"
                }
                """, keycloakId, i, i);

            mockMvc.perform(post("/users")
                            .contentType(JSON)
                            .content(userJson))
                    .andExpect(status().isCreated());
        }

        // test first page
        mockMvc.perform(get("/admin/users")
                        .contentType(JSON)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.first").value(true));

        // test second page
        mockMvc.perform(get("/admin/users")
                        .contentType(JSON)
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.first").value(false));
    }
}