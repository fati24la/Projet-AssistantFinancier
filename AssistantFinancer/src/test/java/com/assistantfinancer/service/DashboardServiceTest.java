package com.assistantfinancer.service;

import com.assistantfinancer.dto.DashboardDto;
import com.assistantfinancer.model.*;
import com.assistantfinancer.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User testUser;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testProfile = new UserProfile();
        testProfile.setId(1L);
        testProfile.setUser(testUser);
        testProfile.setLanguage("FR");
        testProfile.setLevel("BEGINNER");
        testProfile.setPoints(100);
        testProfile.setLevelNumber(2);
        testProfile.setMonthlyIncome(new BigDecimal("5000.00"));
        testProfile.setMonthlyExpenses(new BigDecimal("3000.00"));
        testProfile.setTotalSavings(new BigDecimal("2000.00"));
        testProfile.setTotalDebt(new BigDecimal("1000.00"));
    }

    @Test
    void getDashboard_ShouldCalculateTotalIncomeCorrectly() {
        // Given
        List<Budget> budgets = new ArrayList<>();
        Budget budget = new Budget();
        budget.setAmount(new BigDecimal("1000.00"));
        budget.setSpent(new BigDecimal("200.00"));
        budgets.add(budget);

        List<SavingsGoal> goals = new ArrayList<>();
        SavingsGoal goal = new SavingsGoal();
        goal.setCurrentAmount(new BigDecimal("500.00"));
        goals.add(goal);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(expenseRepository.findByUserAndDateBetween(any(), any(), any())).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUser(testUser)).thenReturn(goals);
        when(budgetRepository.findByUser(testUser)).thenReturn(budgets);
        when(savingsGoalRepository.findByUserAndCompleted(testUser, false)).thenReturn(new ArrayList<>());
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(new ArrayList<>());

        // When
        DashboardDto result = dashboardService.getDashboard(1L);

        // Then
        assertNotNull(result);
        // totalIncome = totalBudgetRemaining + totalSavings
        // totalBudgetRemaining = 1000 - 200 = 800
        // totalSavings = 500
        // totalIncome = 800 + 500 = 1300
        assertEquals(new BigDecimal("1300.00"), result.getTotalIncome());
    }

    @Test
    void getDashboard_ShouldCalculateTotalExpensesCorrectly() {
        // Given
        List<Expense> expenses = new ArrayList<>();
        Expense expense1 = new Expense();
        expense1.setAmount(new BigDecimal("100.00"));
        expense1.setCategory("FOOD");
        expenses.add(expense1);

        Expense expense2 = new Expense();
        expense2.setAmount(new BigDecimal("200.00"));
        expense2.setCategory("TRANSPORT");
        expenses.add(expense2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(expenseRepository.findByUserAndDateBetween(any(), any(), any())).thenReturn(expenses);
        when(savingsGoalRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(budgetRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUserAndCompleted(testUser, false)).thenReturn(new ArrayList<>());
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(new ArrayList<>());

        // When
        DashboardDto result = dashboardService.getDashboard(1L);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("300.00"), result.getTotalExpenses());
    }

    @Test
    void getDashboard_ShouldCalculateTotalSavingsCorrectly() {
        // Given
        List<SavingsGoal> goals = new ArrayList<>();
        SavingsGoal goal1 = new SavingsGoal();
        goal1.setCurrentAmount(new BigDecimal("500.00"));
        goals.add(goal1);

        SavingsGoal goal2 = new SavingsGoal();
        goal2.setCurrentAmount(new BigDecimal("300.00"));
        goals.add(goal2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(expenseRepository.findByUserAndDateBetween(any(), any(), any())).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUser(testUser)).thenReturn(goals);
        when(budgetRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUserAndCompleted(testUser, false)).thenReturn(new ArrayList<>());
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(new ArrayList<>());

        // When
        DashboardDto result = dashboardService.getDashboard(1L);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("800.00"), result.getTotalSavings());
    }

    @Test
    void getDashboard_ShouldCalculateTotalDebtCorrectly() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(expenseRepository.findByUserAndDateBetween(any(), any(), any())).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(budgetRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUserAndCompleted(testUser, false)).thenReturn(new ArrayList<>());
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(new ArrayList<>());

        // When
        DashboardDto result = dashboardService.getDashboard(1L);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("1000.00"), result.getTotalDebt());
    }

    @Test
    void calculateFinancialHealthScore_WithPositiveIncome_ShouldCalculateScore() {
        // Given
        BigDecimal income = new BigDecimal("5000.00");
        BigDecimal expenses = new BigDecimal("3000.00");
        BigDecimal savings = new BigDecimal("2000.00");
        BigDecimal debt = new BigDecimal("1000.00");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(expenseRepository.findByUserAndDateBetween(any(), any(), any())).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(budgetRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUserAndCompleted(testUser, false)).thenReturn(new ArrayList<>());
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(new ArrayList<>());

        // When
        DashboardDto result = dashboardService.getDashboard(1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getFinancialHealthScore());
        assertTrue(result.getFinancialHealthScore().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(result.getFinancialHealthScore().compareTo(BigDecimal.valueOf(100)) <= 0);
    }

    @Test
    void calculateFinancialHealthScore_WithZeroIncome_ShouldReturnDefaultScore() {
        // Given
        testProfile.setMonthlyIncome(BigDecimal.ZERO);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(expenseRepository.findByUserAndDateBetween(any(), any(), any())).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(budgetRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUserAndCompleted(testUser, false)).thenReturn(new ArrayList<>());
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(new ArrayList<>());

        // When
        DashboardDto result = dashboardService.getDashboard(1L);

        // Then
        assertNotNull(result);
        // When income is 0, totalIncome will be 0, so score should be 50
        assertEquals(BigDecimal.valueOf(50), result.getFinancialHealthScore());
    }

    @Test
    void calculateFinancialHealthScore_ShouldCalculateRatios() {
        // Given
        BigDecimal income = new BigDecimal("10000.00");
        BigDecimal expenses = new BigDecimal("5000.00");
        BigDecimal savings = new BigDecimal("3000.00");
        BigDecimal debt = new BigDecimal("2000.00");

        List<Budget> budgets = new ArrayList<>();
        Budget budget = new Budget();
        budget.setAmount(new BigDecimal("10000.00"));
        budget.setSpent(new BigDecimal("5000.00"));
        budgets.add(budget);

        List<SavingsGoal> goals = new ArrayList<>();
        SavingsGoal goal = new SavingsGoal();
        goal.setCurrentAmount(savings);
        goals.add(goal);

        testProfile.setTotalDebt(debt);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(expenseRepository.findByUserAndDateBetween(any(), any(), any())).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUser(testUser)).thenReturn(goals);
        when(budgetRepository.findByUser(testUser)).thenReturn(budgets);
        when(savingsGoalRepository.findByUserAndCompleted(testUser, false)).thenReturn(new ArrayList<>());
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(new ArrayList<>());

        // When
        DashboardDto result = dashboardService.getDashboard(1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getFinancialHealthScore());
    }

    @Test
    void calculateDailyData_ShouldGenerateDailyData() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(expenseRepository.findByUserAndDateBetween(any(), any(), any())).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(budgetRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUserAndCompleted(testUser, false)).thenReturn(new ArrayList<>());
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(new ArrayList<>());

        // When
        DashboardDto result = dashboardService.getDashboard(1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getMonthlyData());
        assertFalse(result.getMonthlyData().isEmpty());
    }

    @Test
    void calculateCategoryExpenses_ShouldGroupByCategory() {
        // Given
        List<Expense> expenses = new ArrayList<>();
        Expense expense1 = new Expense();
        expense1.setCategory("FOOD");
        expense1.setAmount(new BigDecimal("100.00"));
        expenses.add(expense1);

        Expense expense2 = new Expense();
        expense2.setCategory("FOOD");
        expense2.setAmount(new BigDecimal("50.00"));
        expenses.add(expense2);

        Expense expense3 = new Expense();
        expense3.setCategory("TRANSPORT");
        expense3.setAmount(new BigDecimal("75.00"));
        expenses.add(expense3);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(expenseRepository.findByUserAndDateBetween(any(), any(), any())).thenReturn(expenses);
        when(savingsGoalRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(budgetRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUserAndCompleted(testUser, false)).thenReturn(new ArrayList<>());
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(new ArrayList<>());

        // When
        DashboardDto result = dashboardService.getDashboard(1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCategoryExpenses());
        assertEquals(2, result.getCategoryExpenses().size());
    }

    @Test
    void calculateCategoryExpenses_ShouldCalculatePercentages() {
        // Given
        List<Expense> expenses = new ArrayList<>();
        Expense expense1 = new Expense();
        expense1.setCategory("FOOD");
        expense1.setAmount(new BigDecimal("100.00"));
        expenses.add(expense1);

        Expense expense2 = new Expense();
        expense2.setCategory("TRANSPORT");
        expense2.setAmount(new BigDecimal("100.00"));
        expenses.add(expense2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(expenseRepository.findByUserAndDateBetween(any(), any(), any())).thenReturn(expenses);
        when(savingsGoalRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(budgetRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUserAndCompleted(testUser, false)).thenReturn(new ArrayList<>());
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(new ArrayList<>());

        // When
        DashboardDto result = dashboardService.getDashboard(1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCategoryExpenses());
        result.getCategoryExpenses().forEach(cat -> {
            assertEquals(50.0, cat.getPercentage(), 0.01);
        });
    }

    @Test
    void createDefaultProfile_ShouldCreateProfile() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
            UserProfile profile = invocation.getArgument(0);
            profile.setId(1L);
            return profile;
        });
        when(expenseRepository.findByUserAndDateBetween(any(), any(), any())).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(budgetRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUserAndCompleted(testUser, false)).thenReturn(new ArrayList<>());
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(new ArrayList<>());

        // When
        DashboardDto result = dashboardService.getDashboard(1L);

        // Then
        assertNotNull(result);
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void convertToSavingsGoalDto_ShouldCalculateProgress() {
        // Given
        SavingsGoal goal = new SavingsGoal();
        goal.setId(1L);
        goal.setName("Test Goal");
        goal.setTargetAmount(new BigDecimal("1000.00"));
        goal.setCurrentAmount(new BigDecimal("500.00"));
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setCompleted(false);

        List<SavingsGoal> goals = List.of(goal);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(expenseRepository.findByUserAndDateBetween(any(), any(), any())).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(budgetRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(savingsGoalRepository.findByUserAndCompleted(testUser, false)).thenReturn(goals);
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(new ArrayList<>());

        // When
        DashboardDto result = dashboardService.getDashboard(1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getActiveGoals());
        assertEquals(1, result.getActiveGoals().size());
        assertEquals(50.0, result.getActiveGoals().get(0).getProgressPercentage(), 0.01);
    }
}

