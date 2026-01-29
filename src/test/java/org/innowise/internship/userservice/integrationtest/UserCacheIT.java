package org.innowise.internship.userservice.integrationtest;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class UserCacheIT extends AbstractIntegrationTest {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics hibernateStats() {
        SessionFactory sf = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics stats = sf.getStatistics();
        stats.clear();
        return stats;
    }

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE payment_cards RESTART IDENTITY CASCADE");
        clearAllCaches();
    }

    private Long createTestUser() throws Exception {
        String userJson = """
                {
                  "name": "John",
                  "surname": "Doe",
                  "birthDate": "1990-01-01",
                  "email": "john.doe@example.com"
                }
                """;

        String response = mockMvc.perform(post("/users")
                        .contentType(JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return parseId(response);
    }

    @Test
    @DisplayName("Should not execute SQL on second getUserById (cache hit)")
    void getUserById_shouldUseCache_noSecondSql() throws Exception {
        Long userId = createTestUser();
        Statistics stats = hibernateStats();

        // first call -> db
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk());

        long sqlAfterFirstCall = stats.getPrepareStatementCount();

        // sql was executed
        assertTrue(
                sqlAfterFirstCall > 0,
                "First call must hit database"
        );

        // second call -> cache
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk());

        long sqlAfterSecondCall = stats.getPrepareStatementCount();

        assertEquals(
                sqlAfterFirstCall,
                sqlAfterSecondCall,
                "Second call must be served from cache without additional SQL"
        );
    }

    @Test
    @DisplayName("Should evict cache after user deactivation")
    void deactivateUser_shouldEvictCache() throws Exception {
        Long userId = createTestUser();

        // warm up cache
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        // deactivate user -> CacheEvict
        mockMvc.perform(put("/admin/users/" + userId + "/deactivate"))
                .andExpect(status().isNoContent());

        // cache must be evicted, fresh DB read
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Should update cache after user update")
    void updateUser_shouldUpdateCache() throws Exception {
        Long userId = createTestUser();

        // warm up cache
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"));

        String updateJson = """
                {
                  "name": "Jonathan",
                  "surname": "Doe"
                }
                """;

        // update user
        mockMvc.perform(put("/users/" + userId)
                        .contentType(JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jonathan"));

        // second get -> must return updated cached value
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jonathan"));
    }

    @Test
    @DisplayName("Should evict cache after user deletion")
    void deleteUser_shouldEvictCache() throws Exception {
        Long userId = createTestUser();

        // warm up cache
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk());

        // delete user
        mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().isNoContent());

        // cache must be evicted -> 404
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should not use stale cache after deactivation")
    void cache_shouldNotReturnStaleUser() throws Exception {
        Long userId = createTestUser();

        // warm up cache (active = true)
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        // deactivate user
        mockMvc.perform(put("/admin/users/" + userId + "/deactivate"))
                .andExpect(status().isNoContent());

        // business logic must see inactive user
        mockMvc.perform(delete("/users/" + userId + "/cards/1"))
                .andExpect(status().isBadRequest());
    }
}

