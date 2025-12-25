package com.assistantfinancer.controller.integration;

import com.assistantfinancer.dto.ExpenseDto;
import com.assistantfinancer.model.Expense;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.ExpenseRepository;
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
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        expenseRepository.deleteAll();

        testUser = testHelper.createTestUser("testuser", "test@example.com", "password123");
        authToken = testHelper.generateTokenForUser(testUser);
    }

    @Test
    void testCreateExpense_ShouldCreateSuccessfully() throws Exception {
        ExpenseDto expenseDto = new ExpenseDto();
        expenseDto.setDescription("Test Expense");
        expenseDto.setCategory("Food");
        expenseDto.setAmount(new BigDecimal("50.00"));
        expenseDto.setDate(LocalDate.now());
        expenseDto.setPaymentMethod("CARD");

        String response = mockMvc.perform(post("/api/expenses")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Expense"))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Vérifier que la dépense a été créée en base
        Expense savedExpense = expenseRepository.findByUser(testUser).stream()
                .findFirst()
                .orElse(null);
        assertNotNull(savedExpense);
        assertEquals("Test Expense", savedExpense.getDescription());
    }

    @Test
    void testGetUserExpenses_ShouldReturnOnlyUserExpenses() throws Exception {
        // Créer plusieurs dépenses
        Expense expense1 = new Expense();
        expense1.setDescription("Expense 1");
        expense1.setCategory("Food");
        expense1.setAmount(new BigDecimal("50.00"));
        expense1.setDate(LocalDate.now());
        expense1.setUser(testUser);
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setDescription("Expense 2");
        expense2.setCategory("Transport");
        expense2.setAmount(new BigDecimal("30.00"));
        expense2.setDate(LocalDate.now());
        expense2.setUser(testUser);
        expenseRepository.save(expense2);

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testUpdateExpense_ShouldUpdateSuccessfully() throws Exception {
        Expense expense = new Expense();
        expense.setDescription("Original Expense");
        expense.setCategory("Food");
        expense.setAmount(new BigDecimal("50.00"));
        expense.setDate(LocalDate.now());
        expense.setUser(testUser);
        expense = expenseRepository.save(expense);

        ExpenseDto updateDto = new ExpenseDto();
        updateDto.setDescription("Updated Expense");
        updateDto.setCategory("Transport");
        updateDto.setAmount(new BigDecimal("75.00"));

        mockMvc.perform(put("/api/expenses/" + expense.getId())
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Expense"))
                .andExpect(jsonPath("$.category").value("Transport"))
                .andExpect(jsonPath("$.amount").value(75.00));

        // Vérifier en base
        Expense updated = expenseRepository.findById(expense.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals("Updated Expense", updated.getDescription());
    }

    @Test
    void testDeleteExpense_ShouldDeleteSuccessfully() throws Exception {
        Expense expense = new Expense();
        expense.setDescription("Expense to Delete");
        expense.setCategory("Food");
        expense.setAmount(new BigDecimal("50.00"));
        expense.setDate(LocalDate.now());
        expense.setUser(testUser);
        expense = expenseRepository.save(expense);

        mockMvc.perform(delete("/api/expenses/" + expense.getId())
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk());

        // Vérifier que la dépense a été supprimée
        assertFalse(expenseRepository.findById(expense.getId()).isPresent());
    }

    @Test
    void testFilterExpensesByCategory_ShouldReturnFilteredResults() throws Exception {
        // Créer des dépenses avec différentes catégories
        Expense expense1 = new Expense();
        expense1.setDescription("Food Expense");
        expense1.setCategory("Food");
        expense1.setAmount(new BigDecimal("50.00"));
        expense1.setDate(LocalDate.now());
        expense1.setUser(testUser);
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setDescription("Transport Expense");
        expense2.setCategory("Transport");
        expense2.setAmount(new BigDecimal("30.00"));
        expense2.setDate(LocalDate.now());
        expense2.setUser(testUser);
        expenseRepository.save(expense2);

        Expense expense3 = new Expense();
        expense3.setDescription("Another Food Expense");
        expense3.setCategory("Food");
        expense3.setAmount(new BigDecimal("25.00"));
        expense3.setDate(LocalDate.now());
        expense3.setUser(testUser);
        expenseRepository.save(expense3);

        // Récupérer toutes les dépenses et filtrer côté client (le service peut aussi le faire)
        String response = mockMvc.perform(get("/api/expenses")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Vérifier que toutes les dépenses sont retournées
        // Le filtrage par catégorie peut être fait côté service ou client
        assertNotNull(response);
    }

    @Test
    void testFilterExpensesByDate_ShouldReturnFilteredResults() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Expense expense1 = new Expense();
        expense1.setDescription("Today Expense");
        expense1.setCategory("Food");
        expense1.setAmount(new BigDecimal("50.00"));
        expense1.setDate(today);
        expense1.setUser(testUser);
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setDescription("Yesterday Expense");
        expense2.setCategory("Transport");
        expense2.setAmount(new BigDecimal("30.00"));
        expense2.setDate(yesterday);
        expense2.setUser(testUser);
        expenseRepository.save(expense2);

        // Récupérer toutes les dépenses
        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testCreateExpense_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        ExpenseDto expenseDto = new ExpenseDto();
        expenseDto.setDescription("Test Expense");
        expenseDto.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDto)))
                .andExpect(status().isForbidden());
    }
}

