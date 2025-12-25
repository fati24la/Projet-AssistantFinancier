package com.assistantfinancer.service;

import com.assistantfinancer.dto.BudgetDto;
import com.assistantfinancer.model.Budget;
import com.assistantfinancer.model.Expense;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.BudgetRepository;
import com.assistantfinancer.repository.ExpenseRepository;
import com.assistantfinancer.repository.UserRepository;
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
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Budget testBudget;
    private BudgetDto testBudgetDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setName("Test Budget");
        testBudget.setCategory("FOOD");
        testBudget.setAmount(new BigDecimal("1000.00"));
        testBudget.setSpent(BigDecimal.ZERO);
        testBudget.setStartDate(LocalDate.now());
        testBudget.setEndDate(LocalDate.now().plusMonths(1));
        testBudget.setPeriod("MONTHLY");
        testBudget.setUser(testUser);
        testBudget.setCreatedAt(LocalDateTime.now());

        testBudgetDto = new BudgetDto();
        testBudgetDto.setName("Test Budget");
        testBudgetDto.setCategory("FOOD");
        testBudgetDto.setAmount(new BigDecimal("1000.00"));
        testBudgetDto.setStartDate(LocalDate.now());
        testBudgetDto.setEndDate(LocalDate.now().plusMonths(1));
        testBudgetDto.setPeriod("MONTHLY");
    }

    @Test
    void createBudget_WithValidData_ShouldCreateBudget() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        // When
        BudgetDto result = budgetService.createBudget(1L, testBudgetDto);

        // Then
        assertNotNull(result);
        assertEquals("Test Budget", result.getName());
        assertEquals("FOOD", result.getCategory());
        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        assertEquals(BigDecimal.ZERO, result.getSpent());
        verify(userRepository, times(1)).findById(1L);
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void createBudget_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.createBudget(999L, testBudgetDto);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void getUserBudgets_WithValidUser_ShouldReturnBudgets() {
        // Given
        List<Budget> budgets = new ArrayList<>();
        budgets.add(testBudget);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUser(testUser)).thenReturn(budgets);

        // When
        List<BudgetDto> result = budgetService.getUserBudgets(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Budget", result.get(0).getName());
        verify(userRepository, times(1)).findById(1L);
        verify(budgetRepository, times(1)).findByUser(testUser);
    }

    @Test
    void getUserBudgets_WithNoBudgets_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUser(testUser)).thenReturn(new ArrayList<>());

        // When
        List<BudgetDto> result = budgetService.getUserBudgets(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findById(1L);
        verify(budgetRepository, times(1)).findByUser(testUser);
    }

    @Test
    void updateBudget_WithValidData_ShouldUpdateBudget() {
        // Given
        BudgetDto updateDto = new BudgetDto();
        updateDto.setName("Updated Budget");
        updateDto.setCategory("TRANSPORT");
        updateDto.setAmount(new BigDecimal("2000.00"));
        updateDto.setStartDate(LocalDate.now());
        updateDto.setEndDate(LocalDate.now().plusMonths(1));
        updateDto.setPeriod("MONTHLY");

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        // When
        BudgetDto result = budgetService.updateBudget(1L, 1L, updateDto);

        // Then
        assertNotNull(result);
        verify(budgetRepository, times(1)).findById(1L);
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void updateBudget_WithUnauthorizedUser_ShouldThrowException() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testBudget.setUser(otherUser);

        BudgetDto updateDto = new BudgetDto();
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.updateBudget(1L, 1L, updateDto);
        });

        assertEquals("Unauthorized", exception.getMessage());
        verify(budgetRepository, times(1)).findById(1L);
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void updateBudget_WithNonExistentBudget_ShouldThrowException() {
        // Given
        BudgetDto updateDto = new BudgetDto();
        when(budgetRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.updateBudget(1L, 999L, updateDto);
        });

        assertEquals("Budget not found", exception.getMessage());
        verify(budgetRepository, times(1)).findById(999L);
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void deleteBudget_WithValidData_ShouldDeleteBudget() {
        // Given
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));
        doNothing().when(budgetRepository).delete(testBudget);

        // When
        budgetService.deleteBudget(1L, 1L);

        // Then
        verify(budgetRepository, times(1)).findById(1L);
        verify(budgetRepository, times(1)).delete(testBudget);
    }

    @Test
    void deleteBudget_WithUnauthorizedUser_ShouldThrowException() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testBudget.setUser(otherUser);

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.deleteBudget(1L, 1L);
        });

        assertEquals("Unauthorized", exception.getMessage());
        verify(budgetRepository, times(1)).findById(1L);
        verify(budgetRepository, never()).delete(any(Budget.class));
    }

    @Test
    void updateBudgetSpent_WithValidExpenses_ShouldCalculateSpent() {
        // Given
        Expense expense1 = new Expense();
        expense1.setId(1L);
        expense1.setCategory("FOOD");
        expense1.setAmount(new BigDecimal("100.00"));
        expense1.setDate(LocalDate.now());
        expense1.setUser(testUser);

        Expense expense2 = new Expense();
        expense2.setId(2L);
        expense2.setCategory("FOOD");
        expense2.setAmount(new BigDecimal("200.00"));
        expense2.setDate(LocalDate.now());
        expense2.setUser(testUser);

        List<Expense> expenses = List.of(expense1, expense2);

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));
        when(expenseRepository.findByUserAndDateBetween(
                testUser, testBudget.getStartDate(), testBudget.getEndDate()))
                .thenReturn(expenses);
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        // When
        budgetService.updateBudgetSpent(1L);

        // Then
        verify(budgetRepository, times(1)).findById(1L);
        verify(expenseRepository, times(1)).findByUserAndDateBetween(
                testUser, testBudget.getStartDate(), testBudget.getEndDate());
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void updateBudgetSpent_WithFilteredByCategory_ShouldOnlyCountMatchingExpenses() {
        // Given
        Expense foodExpense = new Expense();
        foodExpense.setCategory("FOOD");
        foodExpense.setAmount(new BigDecimal("100.00"));
        foodExpense.setDate(LocalDate.now());
        foodExpense.setUser(testUser);

        Expense transportExpense = new Expense();
        transportExpense.setCategory("TRANSPORT");
        transportExpense.setAmount(new BigDecimal("200.00"));
        transportExpense.setDate(LocalDate.now());
        transportExpense.setUser(testUser);

        List<Expense> expenses = List.of(foodExpense, transportExpense);

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));
        when(expenseRepository.findByUserAndDateBetween(
                testUser, testBudget.getStartDate(), testBudget.getEndDate()))
                .thenReturn(expenses);
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        // When
        budgetService.updateBudgetSpent(1L);

        // Then
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void convertToDto_WithValidBudget_ShouldCalculatePercentage() {
        // Given
        testBudget.setAmount(new BigDecimal("1000.00"));
        testBudget.setSpent(new BigDecimal("250.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUser(testUser)).thenReturn(List.of(testBudget));

        // When
        BudgetDto result = budgetService.getUserBudgets(1L).get(0);

        // Then
        assertNotNull(result);
        assertEquals(25.0, result.getPercentageUsed(), 0.01);
        assertEquals(new BigDecimal("750.00"), result.getRemaining());
    }

    @Test
    void convertToDto_WithZeroAmount_ShouldReturnZeroPercentage() {
        // Given
        testBudget.setAmount(BigDecimal.ZERO);
        testBudget.setSpent(BigDecimal.ZERO);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUser(testUser)).thenReturn(List.of(testBudget));

        // When
        BudgetDto result = budgetService.getUserBudgets(1L).get(0);

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.getPercentageUsed());
    }
}

