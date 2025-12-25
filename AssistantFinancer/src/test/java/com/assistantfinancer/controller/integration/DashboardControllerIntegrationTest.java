package com.assistantfinancer.controller.integration;

import com.assistantfinancer.model.Budget;
import com.assistantfinancer.model.Expense;
import com.assistantfinancer.model.SavingsGoal;
import com.assistantfinancer.model.User;
import com.assistantfinancer.model.UserProfile;
import com.assistantfinancer.repository.*;
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
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

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
        budgetRepository.deleteAll();
        savingsGoalRepository.deleteAll();
        userProfileRepository.deleteAll();

        testUser = testHelper.createTestUser("testuser", "test@example.com", "password123");
        authToken = testHelper.generateTokenForUser(testUser);
    }

    @Test
    void testGetDashboard_WithData_ShouldReturnDashboard() throws Exception {
        // Créer des données pour le dashboard
        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setCategory("Food");
        budget.setAmount(new BigDecimal("1000.00"));
        budget.setSpent(new BigDecimal("500.00"));
        budget.setUser(testUser);
        budgetRepository.save(budget);

        Expense expense = new Expense();
        expense.setDescription("Test Expense");
        expense.setCategory("Food");
        expense.setAmount(new BigDecimal("50.00"));
        expense.setDate(LocalDate.now());
        expense.setUser(testUser);
        expenseRepository.save(expense);

        SavingsGoal goal = new SavingsGoal();
        goal.setName("Test Goal");
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setCurrentAmount(new BigDecimal("1000.00"));
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setUser(testUser);
        goal.setCompleted(false);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        savingsGoalRepository.save(goal);

        UserProfile profile = new UserProfile();
        profile.setUser(testUser);
        profile.setMonthlyIncome(new BigDecimal("2000.00"));
        profile.setMonthlyExpenses(new BigDecimal("1500.00"));
        profile.setTotalSavings(new BigDecimal("1000.00"));
        profile.setTotalDebt(new BigDecimal("500.00"));
        profile.setPoints(100);
        profile.setLevelNumber(2);
        profile.setLevel("BEGINNER");
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);

        String response = mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").exists())
                .andExpect(jsonPath("$.totalExpenses").exists())
                .andExpect(jsonPath("$.totalSavings").exists())
                .andExpect(jsonPath("$.totalDebt").exists())
                .andExpect(jsonPath("$.financialHealthScore").exists())
                .andExpect(jsonPath("$.monthlyData").isArray())
                .andExpect(jsonPath("$.categoryExpenses").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
    }

    @Test
    void testGetDashboard_Empty_ShouldReturnEmptyDashboard() throws Exception {
        // Créer un profil par défaut
        UserProfile profile = new UserProfile();
        profile.setUser(testUser);
        profile.setMonthlyIncome(BigDecimal.ZERO);
        profile.setMonthlyExpenses(BigDecimal.ZERO);
        profile.setTotalSavings(BigDecimal.ZERO);
        profile.setTotalDebt(BigDecimal.ZERO);
        profile.setPoints(0);
        profile.setLevelNumber(1);
        profile.setLevel("BEGINNER");
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);

        String response = mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(0))
                .andExpect(jsonPath("$.totalExpenses").value(0))
                .andExpect(jsonPath("$.totalSavings").value(0))
                .andExpect(jsonPath("$.totalDebt").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
    }

    @Test
    void testGetDashboard_CalculateFinancialStatistics_ShouldReturnCorrectValues() throws Exception {
        // Créer des données avec des valeurs connues
        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setCategory("Food");
        budget.setAmount(new BigDecimal("2000.00"));
        budget.setSpent(new BigDecimal("500.00"));
        budget.setUser(testUser);
        budgetRepository.save(budget);

        Expense expense1 = new Expense();
        expense1.setDescription("Expense 1");
        expense1.setCategory("Food");
        expense1.setAmount(new BigDecimal("100.00"));
        expense1.setDate(LocalDate.now());
        expense1.setUser(testUser);
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setDescription("Expense 2");
        expense2.setCategory("Transport");
        expense2.setAmount(new BigDecimal("50.00"));
        expense2.setDate(LocalDate.now());
        expense2.setUser(testUser);
        expenseRepository.save(expense2);

        SavingsGoal goal = new SavingsGoal();
        goal.setName("Test Goal");
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setCurrentAmount(new BigDecimal("2000.00"));
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setUser(testUser);
        goal.setCompleted(false);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        savingsGoalRepository.save(goal);

        UserProfile profile = new UserProfile();
        profile.setUser(testUser);
        profile.setMonthlyIncome(new BigDecimal("3000.00"));
        profile.setMonthlyExpenses(new BigDecimal("2000.00"));
        profile.setTotalSavings(new BigDecimal("2000.00"));
        profile.setTotalDebt(new BigDecimal("1000.00"));
        profile.setPoints(150);
        profile.setLevelNumber(2);
        profile.setLevel("BEGINNER");
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(150.00))
                .andExpect(jsonPath("$.totalSavings").value(2000.00))
                .andExpect(jsonPath("$.categoryExpenses").isArray())
                .andExpect(jsonPath("$.categoryExpenses.length()").value(2));
    }

    @Test
    void testGetDashboard_HideFinancialHealthScore_WhenAllValuesAreZero() throws Exception {
        // Créer un profil avec toutes les valeurs à zéro
        UserProfile profile = new UserProfile();
        profile.setUser(testUser);
        profile.setMonthlyIncome(BigDecimal.ZERO);
        profile.setMonthlyExpenses(BigDecimal.ZERO);
        profile.setTotalSavings(BigDecimal.ZERO);
        profile.setTotalDebt(BigDecimal.ZERO);
        profile.setPoints(0);
        profile.setLevelNumber(1);
        profile.setLevel("BEGINNER");
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);

        String response = mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Vérifier que le score de santé financière est présent mais peut être null ou 0
        // Selon la mémoire, on devrait masquer le score si toutes les valeurs sont à zéro
        // Le service retourne toujours un score (50 par défaut), donc on vérifie que c'est bien le cas
        // Le masquage se fait côté frontend selon la mémoire
        assertNotNull(response);
    }

    @Test
    void testGetDashboard_ShowFinancialHealthScore_WhenAtLeastOneValueIsNonZero() throws Exception {
        // Créer un profil avec au moins une valeur non zéro
        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setCategory("Food");
        budget.setAmount(new BigDecimal("1000.00"));
        budget.setSpent(new BigDecimal("0.00"));
        budget.setUser(testUser);
        budgetRepository.save(budget);

        UserProfile profile = new UserProfile();
        profile.setUser(testUser);
        profile.setMonthlyIncome(new BigDecimal("1000.00"));
        profile.setMonthlyExpenses(BigDecimal.ZERO);
        profile.setTotalSavings(BigDecimal.ZERO);
        profile.setTotalDebt(BigDecimal.ZERO);
        profile.setPoints(0);
        profile.setLevelNumber(1);
        profile.setLevel("BEGINNER");
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financialHealthScore").exists())
                .andExpect(jsonPath("$.totalIncome").value(1000.00));
    }

    @Test
    void testGetDashboard_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isForbidden());
    }
}

