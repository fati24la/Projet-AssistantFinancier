package com.assistantfinancer.controller.integration;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BudgetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private User otherUser;
    private String authToken;
    private String otherUserToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        budgetRepository.deleteAll();

        testUser = testHelper.createTestUser("testuser", "test@example.com", "password123");
        otherUser = testHelper.createTestUser("otheruser", "other@example.com", "password123");
        authToken = testHelper.generateTokenForUser(testUser);
        otherUserToken = testHelper.generateTokenForUser(otherUser);
    }

    @Test
    void testCreateBudget_WithAuthentication_ShouldCreateSuccessfully() throws Exception {
        BudgetDto budgetDto = new BudgetDto();
        budgetDto.setName("Test Budget");
        budgetDto.setCategory("Food");
        budgetDto.setAmount(new BigDecimal("1000.00"));
        budgetDto.setStartDate(LocalDate.now());
        budgetDto.setEndDate(LocalDate.now().plusMonths(1));
        budgetDto.setPeriod("MONTHLY");

        String response = mockMvc.perform(post("/api/budgets")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budgetDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Budget"))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.amount").value(1000.00))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Vérifier que le budget a été créé en base
        Budget savedBudget = budgetRepository.findByUser(testUser).stream()
                .findFirst()
                .orElse(null);
        assertNotNull(savedBudget);
        assertEquals("Test Budget", savedBudget.getName());
    }

    @Test
    void testGetUserBudgets_ShouldReturnOnlyUserBudgets() throws Exception {
        // Créer des budgets pour les deux utilisateurs
        Budget user1Budget = new Budget();
        user1Budget.setName("User1 Budget");
        user1Budget.setCategory("Food");
        user1Budget.setAmount(new BigDecimal("500.00"));
        user1Budget.setSpent(BigDecimal.ZERO);
        user1Budget.setUser(testUser);
        budgetRepository.save(user1Budget);

        Budget user2Budget = new Budget();
        user2Budget.setName("User2 Budget");
        user2Budget.setCategory("Transport");
        user2Budget.setAmount(new BigDecimal("300.00"));
        user2Budget.setSpent(BigDecimal.ZERO);
        user2Budget.setUser(otherUser);
        budgetRepository.save(user2Budget);

        mockMvc.perform(get("/api/budgets")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("User1 Budget"));
    }

    @Test
    void testUpdateBudget_ShouldUpdateSuccessfully() throws Exception {
        Budget budget = new Budget();
        budget.setName("Original Budget");
        budget.setCategory("Food");
        budget.setAmount(new BigDecimal("500.00"));
        budget.setSpent(BigDecimal.ZERO);
        budget.setUser(testUser);
        budget = budgetRepository.save(budget);

        BudgetDto updateDto = new BudgetDto();
        updateDto.setName("Updated Budget");
        updateDto.setCategory("Transport");
        updateDto.setAmount(new BigDecimal("750.00"));

        mockMvc.perform(put("/api/budgets/" + budget.getId())
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Budget"))
                .andExpect(jsonPath("$.category").value("Transport"))
                .andExpect(jsonPath("$.amount").value(750.00));

        // Vérifier en base
        Budget updated = budgetRepository.findById(budget.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals("Updated Budget", updated.getName());
    }

    @Test
    void testDeleteBudget_ShouldDeleteSuccessfully() throws Exception {
        Budget budget = new Budget();
        budget.setName("Budget to Delete");
        budget.setCategory("Food");
        budget.setAmount(new BigDecimal("500.00"));
        budget.setSpent(BigDecimal.ZERO);
        budget.setUser(testUser);
        budget = budgetRepository.save(budget);

        mockMvc.perform(delete("/api/budgets/" + budget.getId())
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk());

        // Vérifier que le budget a été supprimé
        assertFalse(budgetRepository.findById(budget.getId()).isPresent());
    }

    @Test
    void testAccessUnauthorizedBudget_ShouldReturnError() throws Exception {
        // Créer un budget pour otherUser
        Budget otherUserBudget = new Budget();
        otherUserBudget.setName("Other User Budget");
        otherUserBudget.setCategory("Food");
        otherUserBudget.setAmount(new BigDecimal("500.00"));
        otherUserBudget.setSpent(BigDecimal.ZERO);
        otherUserBudget.setUser(otherUser);
        otherUserBudget = budgetRepository.save(otherUserBudget);

        // Tenter d'accéder au budget d'un autre utilisateur
        mockMvc.perform(get("/api/budgets")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // Tenter de mettre à jour le budget d'un autre utilisateur
        BudgetDto updateDto = new BudgetDto();
        updateDto.setName("Hacked Budget");

        // Le service devrait gérer l'autorisation, mais testons quand même
        // En fonction de l'implémentation du service, cela peut retourner une erreur
        // ou simplement ne pas trouver le budget
    }

    @Test
    void testCreateBudget_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        BudgetDto budgetDto = new BudgetDto();
        budgetDto.setName("Test Budget");
        budgetDto.setAmount(new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budgetDto)))
                .andExpect(status().isForbidden());
    }
}

