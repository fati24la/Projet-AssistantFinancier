package com.assistantfinancer.service;

import com.assistantfinancer.dto.ExpenseDto;
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
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private ExpenseService expenseService;

    private User testUser;
    private Expense testExpense;
    private ExpenseDto testExpenseDto;
    private Budget testBudget;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setCategory("FOOD");
        testBudget.setStartDate(LocalDate.now().minusDays(1));
        testBudget.setEndDate(LocalDate.now().plusDays(30));
        testBudget.setUser(testUser);

        testExpense = new Expense();
        testExpense.setId(1L);
        testExpense.setDescription("Test Expense");
        testExpense.setCategory("FOOD");
        testExpense.setAmount(new BigDecimal("50.00"));
        testExpense.setDate(LocalDate.now());
        testExpense.setPaymentMethod("CARD");
        testExpense.setUser(testUser);
        testExpense.setCreatedAt(LocalDateTime.now());

        testExpenseDto = new ExpenseDto();
        testExpenseDto.setDescription("Test Expense");
        testExpenseDto.setCategory("FOOD");
        testExpenseDto.setAmount(new BigDecimal("50.00"));
        testExpenseDto.setDate(LocalDate.now());
        testExpenseDto.setPaymentMethod("CARD");
    }

    @Test
    void createExpense_WithValidData_ShouldCreateExpense() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUser(testUser)).thenReturn(new ArrayList<>());
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        // When
        ExpenseDto result = expenseService.createExpense(1L, testExpenseDto);

        // Then
        assertNotNull(result);
        assertEquals("Test Expense", result.getDescription());
        assertEquals("FOOD", result.getCategory());
        assertEquals(new BigDecimal("50.00"), result.getAmount());
        verify(userRepository, times(1)).findById(1L);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void createExpense_WithMatchingBudget_ShouldAssociateBudget() {
        // Given
        List<Budget> budgets = List.of(testBudget);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUser(testUser)).thenReturn(budgets);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            expense.setId(1L);
            expense.setBudget(testBudget);
            return expense;
        });

        // When
        ExpenseDto result = expenseService.createExpense(1L, testExpenseDto);

        // Then
        assertNotNull(result);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void createExpense_WithAssociatedBudget_ShouldUpdateBudget() {
        // Given
        List<Budget> budgets = List.of(testBudget);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUser(testUser)).thenReturn(budgets);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            expense.setId(1L);
            expense.setBudget(testBudget);
            return expense;
        });

        // When
        expenseService.createExpense(1L, testExpenseDto);

        // Then
        verify(budgetService, times(1)).updateBudgetSpent(testBudget.getId());
    }

    @Test
    void getUserExpenses_WithValidUser_ShouldReturnExpenses() {
        // Given
        List<Expense> expenses = List.of(testExpense);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(expenseRepository.findByUser(testUser)).thenReturn(expenses);

        // When
        List<ExpenseDto> result = expenseService.getUserExpenses(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Expense", result.get(0).getDescription());
        verify(userRepository, times(1)).findById(1L);
        verify(expenseRepository, times(1)).findByUser(testUser);
    }

    @Test
    void updateExpense_WithValidData_ShouldUpdateExpense() {
        // Given
        ExpenseDto updateDto = new ExpenseDto();
        updateDto.setDescription("Updated Expense");
        updateDto.setCategory("TRANSPORT");
        updateDto.setAmount(new BigDecimal("75.00"));
        updateDto.setDate(LocalDate.now());
        updateDto.setPaymentMethod("CASH");

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        // When
        ExpenseDto result = expenseService.updateExpense(1L, 1L, updateDto);

        // Then
        assertNotNull(result);
        verify(expenseRepository, times(1)).findById(1L);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void updateExpense_WithUnauthorizedUser_ShouldThrowException() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testExpense.setUser(otherUser);

        ExpenseDto updateDto = new ExpenseDto();
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseService.updateExpense(1L, 1L, updateDto);
        });

        assertEquals("Unauthorized", exception.getMessage());
        verify(expenseRepository, times(1)).findById(1L);
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void deleteExpense_WithValidData_ShouldDeleteExpense() {
        // Given
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        doNothing().when(expenseRepository).delete(testExpense);

        // When
        expenseService.deleteExpense(1L, 1L);

        // Then
        verify(expenseRepository, times(1)).findById(1L);
        verify(expenseRepository, times(1)).delete(testExpense);
    }

    @Test
    void deleteExpense_WithUnauthorizedUser_ShouldThrowException() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testExpense.setUser(otherUser);

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseService.deleteExpense(1L, 1L);
        });

        assertEquals("Unauthorized", exception.getMessage());
        verify(expenseRepository, times(1)).findById(1L);
        verify(expenseRepository, never()).delete(any(Expense.class));
    }

    @Test
    void convertToDto_WithValidExpense_ShouldConvertCorrectly() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(expenseRepository.findByUser(testUser)).thenReturn(List.of(testExpense));

        // When
        List<ExpenseDto> result = expenseService.getUserExpenses(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        ExpenseDto dto = result.get(0);
        assertEquals(testExpense.getId(), dto.getId());
        assertEquals(testExpense.getDescription(), dto.getDescription());
        assertEquals(testExpense.getCategory(), dto.getCategory());
        assertEquals(testExpense.getAmount(), dto.getAmount());
        assertEquals(testExpense.getDate(), dto.getDate());
        assertEquals(testExpense.getPaymentMethod(), dto.getPaymentMethod());
    }
}

