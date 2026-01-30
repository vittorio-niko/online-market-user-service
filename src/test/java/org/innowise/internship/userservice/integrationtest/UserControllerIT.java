package org.innowise.internship.userservice.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.innowise.internship.userservice.controller.security.UserSecurity;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class UserControllerIT extends AbstractIntegrationTest {
    @MockBean
    private UserSecurity userSecurity;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE payment_cards RESTART IDENTITY CASCADE");
        clearAllCaches();
        when(userSecurity.isOwner(anyLong())).thenReturn(true);
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

        String response = mockMvc.perform(post("/users/" + userId + "/cards")
                        .contentType(JSON)
                        .content(cardJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return parseId(response);
    }

    @Test
    @DisplayName("Should create user successfully")
    void createUser_shouldCreateSuccessfully() throws Exception {
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

        mockMvc.perform(post("/users")
                        .contentType(JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.surname").value("Smith"))
                .andExpect(jsonPath("$.email").value("alice.smith@example.com"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.birthDate").value("1985-05-15"));
    }

    @Test
    @DisplayName("Should return conflict when creating user with duplicate email")
    void createUser_shouldReturnConflictForDuplicateEmail() throws Exception {
        String keycloakId1 = UUID.randomUUID().toString();
        String keycloakId2 = UUID.randomUUID().toString();

        String userJson1 = String.format("""
                {
                  "keycloakId": "%s",
                  "name": "John",
                  "surname": "Doe",
                  "birthDate": "1990-01-01",
                  "email": "john.doe@example.com"
                }
                """, keycloakId1);

        mockMvc.perform(post("/users")
                        .contentType(JSON)
                        .content(userJson1))
                .andExpect(status().isCreated());

        String duplicateUserJson = String.format("""
                {
                  "keycloakId": "%s",
                  "name": "John",
                  "surname": "Smith",
                  "birthDate": "1995-05-15",
                  "email": "john.doe@example.com"
                }
                """, keycloakId2);

        mockMvc.perform(post("/users")
                        .contentType(JSON)
                        .content(duplicateUserJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("email")));
    }

    @Test
    @DisplayName("Should return bad request when creating user with invalid data")
    void createUser_shouldReturnBadRequestForInvalidData() throws Exception {
        // test invalid email
        String invalidEmailJson = String.format("""
                {
                  "keycloakId": "%s",
                  "name": "John",
                  "surname": "Doe",
                  "birthDate": "1990-01-01",
                  "email": "invalid-email"
                }
                """, UUID.randomUUID());

        mockMvc.perform(post("/users")
                        .contentType(JSON)
                        .content(invalidEmailJson))
                .andExpect(status().isBadRequest());

        // test future birthdate
        String futureDateJson = String.format("""
                {
                  "keycloakId": "%s",
                  "name": "John",
                  "surname": "Doe",
                  "birthDate": "2050-01-01",
                  "email": "john@example.com"
                }
                """, UUID.randomUUID());

        mockMvc.perform(post("/users")
                        .contentType(JSON)
                        .content(futureDateJson))
                .andExpect(status().isBadRequest());

        // test empty name
        String emptyNameJson = String.format("""
                {
                  "keycloakId": "%s",
                  "name": "",
                  "surname": "Doe",
                  "birthDate": "1990-01-01",
                  "email": "john@example.com"
                }
                """, UUID.randomUUID());

        mockMvc.perform(post("/users")
                        .contentType(JSON)
                        .content(emptyNameJson))
                .andExpect(status().isBadRequest());

        String missingKeycloakIdJson = """
                {
                  "name": "John",
                  "surname": "Doe",
                  "birthDate": "1990-01-01",
                  "email": "john@example.com"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(JSON)
                        .content(missingKeycloakIdJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get user by id successfully")
    void getUserById_shouldGetSuccessfully() throws Exception {
        Long userId = createTestUser();

        mockMvc.perform(get("/users/" + userId)
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        mockMvc.perform(get("/users/" + userId)
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should return not found when getting non-existent user by id")
    void getUserById_shouldReturnNotFoundForNonExistentUser() throws Exception {
        mockMvc.perform(get("/users/9999")
                        .contentType(JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUser_shouldUpdateSuccessfully() throws Exception {
        Long userId = createTestUser();

        String updateJson = """
                {
                  "name": "Jonathan",
                  "surname": "Doeling",
                  "birthDate": "1990-01-01"
                }
                """;

        mockMvc.perform(put("/users/" + userId)
                        .contentType(JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jonathan"))
                .andExpect(jsonPath("$.surname").value("Doeling"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should return not found when updating non-existent user")
    void updateUser_shouldReturnNotFoundForNonExistentUser() throws Exception {
        String updateJson = """
                {
                  "name": "Jonathan",
                  "surname": "Doeling"
                }
                """;

        mockMvc.perform(put("/users/999999")
                        .contentType(JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return bad request when updating user with invalid data")
    void updateUser_shouldReturnBadRequestForInvalidData() throws Exception {
        Long userId = createTestUser();

        // test future birthdate
        String invalidDateJson = """
                {
                  "birthDate": "2050-01-01"
                }
                """;

        mockMvc.perform(put("/users/" + userId)
                        .contentType(JSON)
                        .content(invalidDateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_shouldDeleteSuccessfully() throws Exception {
        Long userId = createTestUser();

        mockMvc.perform(delete("/users/" + userId)
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // verify user is deleted
        mockMvc.perform(get("/users/" + userId)
                        .contentType(JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found when deleting non-existent user")
    void deleteUser_shouldReturnNotFoundForNonExistentUser() throws Exception {
        mockMvc.perform(delete("/users/999999")
                        .contentType(JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create payment card successfully")
    void createPaymentCard_shouldCreateSuccessfully() throws Exception {
        Long userId = createTestUser();

        String cardJson = String.format("""
                {
                  "userId": %d,
                  "number": "4111111111111357",
                  "holder": "JOHN DOE",
                  "expirationDate": "%s"
                }
                """, userId, LocalDate.now().plusYears(2).format(DATE_FORMATTER));

        mockMvc.perform(post("/users/" + userId + "/cards")
                        .contentType(JSON)
                        .content(cardJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.maskedNumber").value("************1357"))
                .andExpect(jsonPath("$.holder").value("JOHN DOE"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should return bad request when creating card with invalid card number")
    void createPaymentCard_shouldReturnBadRequestForInvalidCardNumber() throws Exception {
        Long userId = createTestUser();

        // test too short number
        String shortNumberJson = String.format("""
                {
                  "userId": %d,
                  "number": "123456789012345",
                  "holder": "JOHN DOE",
                  "expirationDate": "%s"
                }
                """, userId, LocalDate.now().plusYears(1).format(DATE_FORMATTER));

        mockMvc.perform(post("/users/" + userId + "/cards")
                        .contentType(JSON)
                        .content(shortNumberJson))
                .andExpect(status().isBadRequest());

        // test non-numeric number
        String nonNumericJson = String.format("""
                {
                  "userId": %d,
                  "number": "4111-1111-1111-1111",
                  "holder": "JOHN DOE",
                  "expirationDate": "%s"
                }
                """, userId, LocalDate.now().plusYears(1).format(DATE_FORMATTER));

        mockMvc.perform(post("/users/" + userId + "/cards")
                        .contentType(JSON)
                        .content(nonNumericJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when creating expired card")
    void createPaymentCard_shouldReturnBadRequestForExpiredCard() throws Exception {
        Long userId = createTestUser();

        String expiredCardJson = String.format("""
                {
                  "userId": %d,
                  "number": "4111111111111111",
                  "holder": "JOHN DOE",
                  "expirationDate": "%s"
                }
                """, userId, LocalDate.now().minusMonths(1).format(DATE_FORMATTER));

        mockMvc.perform(post("/users/" + userId + "/cards")
                        .contentType(JSON)
                        .content(expiredCardJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return conflict when creating card with duplicate card number")
    void createPaymentCard_shouldReturnConflictForDuplicateCardNumber() throws Exception {
        Long userId = createTestUser();
        createTestPaymentCard(userId);

        String duplicateCardJson = String.format("""
                {
                  "userId": %d,
                  "number": "4111111111111111",
                  "holder": "JANE DOE",
                  "expirationDate": "%s"
                }
                """, userId, LocalDate.now().plusYears(3).format(DATE_FORMATTER));

        mockMvc.perform(post("/users/" + userId + "/cards")
                        .contentType(JSON)
                        .content(duplicateCardJson))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return bad request when user reaches maximum card limit")
    void createPaymentCard_shouldReturnBadRequestForMaxCardsLimit() throws Exception {
        Long userId = createTestUser();

        // create 5 cards
        for (int i = 0; i < 5; i++) {
            String cardJson = String.format("""
                {
                  "userId": %d,
                  "number": "411111111111111%d",
                  "holder": "JOHN DOE %d",
                  "expirationDate": "%s"
                }
                """, userId, i, i, LocalDate.now().plusYears(1).format(DATE_FORMATTER));

            mockMvc.perform(post("/users/" + userId + "/cards")
                            .contentType(JSON)
                            .content(cardJson))
                    .andExpect(status().isCreated());
        }

        // try to create 6th card
        String sixthCardJson = String.format("""
                {
                  "userId": %d,
                  "number": "4111111111111125",
                  "holder": "JOHN DOE 6",
                  "expirationDate": "%s"
                }
                """, userId, LocalDate.now().plusYears(1).format(DATE_FORMATTER));

        mockMvc.perform(post("/users/" + userId + "/cards")
                        .contentType(JSON)
                        .content(sixthCardJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all user cards successfully")
    void getUserCards_shouldGetAllCardsSuccessfully() throws Exception {
        Long userId = createTestUser();
        Long cardId1 = createTestPaymentCard(userId);

        // Create second card
        String secondCardJson = String.format("""
                {
                  "userId": %d,
                  "number": "4222222222222222",
                  "holder": "JOHN DOE",
                  "expirationDate": "%s"
                }
                """, userId, LocalDate.now().plusYears(1).format(DATE_FORMATTER));

        mockMvc.perform(post("/users/" + userId + "/cards")
                        .contentType(JSON)
                        .content(secondCardJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/users/" + userId + "/cards")
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].maskedNumber").value("************1111"))
                .andExpect(jsonPath("$[1].maskedNumber").value("************2222"));
    }

    @Test
    @DisplayName("Should return empty list when user has no cards")
    void getUserCards_shouldReturnEmptyListForNoCards() throws Exception {
        Long userId = createTestUser();

        mockMvc.perform(get("/users/" + userId + "/cards")
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }

    @Test
    @DisplayName("Should get payment card by id successfully")
    void getUserCardById_shouldGetSuccessfully() throws Exception {
        Long userId = createTestUser();
        Long cardId = createTestPaymentCard(userId);

        mockMvc.perform(get("/users/" + userId + "/cards/" + cardId)
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.maskedNumber").value("************1111"))
                .andExpect(jsonPath("$.holder").value("JOHN DOE"));
    }

    @Test
    @DisplayName("Should return not found when getting non-existent payment card")
    void getUserCardById_shouldReturnNotFoundForNonExistentCard() throws Exception {
        Long userId = createTestUser();

        mockMvc.perform(get("/users/" + userId + "/cards/999999")
                        .contentType(JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should activate payment card successfully")
    void activatePaymentCard_shouldActivateSuccessfully() throws Exception {
        Long userId = createTestUser();
        Long cardId = createTestPaymentCard(userId);

        mockMvc.perform(put("/users/" + userId + "/cards/" + cardId + "/activate")
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // verify card is active
        mockMvc.perform(get("/users/" + userId + "/cards/" + cardId)
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should deactivate payment card successfully")
    void deactivatePaymentCard_shouldDeactivateSuccessfully() throws Exception {
        Long userId = createTestUser();
        Long cardId = createTestPaymentCard(userId);

        mockMvc.perform(put("/users/" + userId + "/cards/" + cardId + "/deactivate")
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // verify card is deactivated
        mockMvc.perform(get("/users/" + userId + "/cards/" + cardId)
                        .contentType(JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Should delete payment card successfully")
    void deletePaymentCard_shouldDeleteSuccessfully() throws Exception {
        Long userId = createTestUser();
        Long cardId = createTestPaymentCard(userId);

        mockMvc.perform(delete("/users/" + userId + "/cards/" + cardId)
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // verify card is deleted
        mockMvc.perform(get("/users/" + userId + "/cards/" + cardId)
                        .contentType(JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found when deleting non-existent payment card")
    void deletePaymentCard_shouldReturnNotFoundForNonExistentCard() throws Exception {
        Long userId = createTestUser();

        mockMvc.perform(delete("/users/" + userId + "/cards/9999999999")
                        .contentType(JSON))
                .andExpect(status().isNotFound());
    }

    // edge cases
    @Test
    @DisplayName("Should return bad request when creating card for inactive user")
    void createPaymentCard_shouldReturnBadRequestForInactiveUser() throws Exception {
        Long userId = createTestUser();

        // deactivate user
        mockMvc.perform(put("/admin/users/" + userId + "/deactivate")
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // try to create card for inactive user
        String cardJson = String.format("""
                {
                  "userId": %d,
                  "number": "4111111111111111",
                  "holder": "JOHN DOE",
                  "expirationDate": "%s"
                }
                """, userId, LocalDate.now().plusYears(1).format(DATE_FORMATTER));

        mockMvc.perform(post("/users/" + userId + "/cards")
                        .contentType(JSON)
                        .content(cardJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when deleting card for inactive user")
    void deletePaymentCard_shouldReturnBadRequestForInactiveUser() throws Exception {
        Long userId = createTestUser();
        Long cardId = createTestPaymentCard(userId);

        // deactivate user
        mockMvc.perform(put("/admin/users/" + userId + "/deactivate")
                        .contentType(JSON))
                .andExpect(status().isNoContent());

        // try to delete card for inactive user
        mockMvc.perform(delete("/users/" + userId + "/cards/" + cardId)
                        .contentType(JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when creating user with empty request body")
    void createUser_shouldReturnBadRequestForEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when creating user with malformed JSON")
    void createUser_shouldReturnBadRequestForMalformedJSON() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }
}