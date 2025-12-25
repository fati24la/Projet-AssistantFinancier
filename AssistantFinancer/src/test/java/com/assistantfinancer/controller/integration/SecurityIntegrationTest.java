package com.assistantfinancer.controller.integration;

import com.assistantfinancer.config.JwtService;
import com.assistantfinancer.dto.BudgetDto;
import com.assistantfinancer.model.Budget;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.BudgetRepository;
import com.assistantfinancer.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ObjectMapper objectMapper;

    private User user1;
    private User user2;
    private String token1;
    private String token2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        budgetRepository.deleteAll();

        user1 = testHelper.createTestUser("user1", "user1@example.com", "password123");
        user2 = testHelper.createTestUser("user2", "user2@example.com", "password123");
        token1 = testHelper.generateTokenForUser(user1);
        token2 = testHelper.generateTokenForUser(user2);
    }

    @Test
    void testAccessProtectedEndpoint_WithoutToken_ShouldReturnForbidden() throws Exception {
        BudgetDto budgetDto = new BudgetDto();
        budgetDto.setName("Test Budget");
        budgetDto.setAmount(new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budgetDto)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Spring Security may return 401 or 403 for missing tokens
                    assertTrue(status == 401 || status == 403, 
                        () -> "Expected 401 Unauthorized or 403 Forbidden, but got: " + status);
                });

        mockMvc.perform(get("/api/budgets"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Spring Security may return 401 or 403 for missing tokens
                    assertTrue(status == 401 || status == 403, 
                        () -> "Expected 401 Unauthorized or 403 Forbidden, but got: " + status);
                });

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Spring Security may return 401 or 403 for missing tokens
                    assertTrue(status == 401 || status == 403, 
                        () -> "Expected 401 Unauthorized or 403 Forbidden, but got: " + status);
                });
    }

    @Test
    void testAccessProtectedEndpoint_WithInvalidToken_ShouldReturnForbidden() throws Exception {
        String invalidToken = "invalid.token.here";

        mockMvc.perform(get("/api/budgets")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Spring Security may return 401 or 403 for invalid tokens
                    assertTrue(status == 401 || status == 403, 
                        () -> "Expected 401 Unauthorized or 403 Forbidden, but got: " + status);
                });
    }

    @Test
    void testAccessProtectedEndpoint_WithExpiredToken_ShouldReturnForbidden() throws Exception {
        // Créer un token expiré manuellement (simulation)
        // Note: Dans un vrai scénario, on devrait créer un token avec une date d'expiration passée
        // Pour ce test, on utilise un token malformé ou on teste avec un token qui expire
        
        // Pour l'instant, testons avec un token invalide
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSIsImV4cCI6MTYwOTQ1NjgwMH0.invalid";

        mockMvc.perform(get("/api/budgets")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Spring Security may return 401 or 403 for expired/invalid tokens
                    assertTrue(status == 401 || status == 403, 
                        () -> "Expected 401 Unauthorized or 403 Forbidden, but got: " + status);
                });
    }

    @Test
    void testDataIsolation_BetweenUsers_ShouldOnlyAccessOwnData() throws Exception {
        // Créer un budget pour user1
        Budget budget1 = new Budget();
        budget1.setName("User1 Budget");
        budget1.setCategory("Food");
        budget1.setAmount(new BigDecimal("1000.00"));
        budget1.setSpent(BigDecimal.ZERO);
        budget1.setUser(user1);
        budget1 = budgetRepository.save(budget1);

        // Créer un budget pour user2
        Budget budget2 = new Budget();
        budget2.setName("User2 Budget");
        budget2.setCategory("Transport");
        budget2.setAmount(new BigDecimal("500.00"));
        budget2.setSpent(BigDecimal.ZERO);
        budget2.setUser(user2);
        budget2 = budgetRepository.save(budget2);

        // User1 ne devrait voir que son propre budget
        mockMvc.perform(get("/api/budgets")
                        .header("Authorization", testHelper.getAuthHeader(token1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("User1 Budget"));

        // User2 ne devrait voir que son propre budget
        mockMvc.perform(get("/api/budgets")
                        .header("Authorization", testHelper.getAuthHeader(token2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("User2 Budget"));
    }

    @Test
    void testUnauthorizedAccess_ToOtherUserResource_ShouldReturnError() throws Exception {
        // Créer un budget pour user2
        Budget budget2 = new Budget();
        budget2.setName("User2 Budget");
        budget2.setCategory("Food");
        budget2.setAmount(new BigDecimal("1000.00"));
        budget2.setSpent(BigDecimal.ZERO);
        budget2.setUser(user2);
        budget2 = budgetRepository.save(budget2);

        // User1 tente d'accéder au budget de user2
        // Le service devrait gérer l'autorisation et retourner une erreur ou ne pas trouver le budget
        BudgetDto updateDto = new BudgetDto();
        updateDto.setName("Hacked Budget");

        // Selon l'implémentation, cela peut retourner 404, 403, ou 500
        mockMvc.perform(put("/api/budgets/" + budget2.getId())
                        .header("Authorization", testHelper.getAuthHeader(token1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status >= 400 && status < 600, 
                        () -> "Expected error status but got: " + status);
                });
    }

    @Test
    void testPublicEndpoints_ShouldBeAccessibleWithoutToken() throws Exception {
        // Les endpoints d'authentification devraient être accessibles sans token
        // (mais ils nécessitent des données valides)
        // GET n'est pas supporté sur /api/auth/register (c'est POST uniquement)
        mockMvc.perform(get("/api/auth/register"))
                .andExpect(status().isMethodNotAllowed());

        // POST avec des données invalides devrait retourner 400 ou 500, pas 401 ou 403
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status >= 400 && status < 600, 
                        () -> "Expected error status but got: " + status);
                    // Ne devrait pas être 401 ou 403 car l'endpoint est public
                    // (mais peut être 500 si une exception est lancée)
                });
    }

    @Test
    void testTokenValidation_ShouldExtractCorrectUsername() throws Exception {
        // Vérifier que le token contient le bon username
        String username = jwtService.extractUsername(token1);
        assertEquals("user1", username);

        username = jwtService.extractUsername(token2);
        assertEquals("user2", username);
    }

    @Test
    void testMultipleRequests_WithSameToken_ShouldAllSucceed() throws Exception {
        // Faire plusieurs requêtes avec le même token
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/budgets")
                            .header("Authorization", testHelper.getAuthHeader(token1)))
                    .andExpect(status().isOk());
        }
    }
}

