package com.assistantfinancer.service;

import com.assistantfinancer.dto.ExpenseDto;
import com.assistantfinancer.model.Budget;
import com.assistantfinancer.model.Expense;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.BudgetRepository;
import com.assistantfinancer.repository.ExpenseRepository;
import com.assistantfinancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetService budgetService;

    public ExpenseDto createExpense(Long userId, ExpenseDto expenseDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Expense expense = new Expense();
        expense.setDescription(expenseDto.getDescription());
        expense.setCategory(expenseDto.getCategory());
        expense.setAmount(expenseDto.getAmount());
        expense.setDate(expenseDto.getDate());
        expense.setPaymentMethod(expenseDto.getPaymentMethod());
        expense.setUser(user);
        expense.setCreatedAt(LocalDateTime.now());

        // Associer au budget si disponible
        if (expenseDto.getCategory() != null) {
            List<Budget> budgets = budgetRepository.findByUser(user);
            budgets.stream()
                    .filter(b -> b.getCategory().equals(expenseDto.getCategory()))
                    .filter(b -> !expenseDto.getDate().isBefore(b.getStartDate()))
                    .filter(b -> !expenseDto.getDate().isAfter(b.getEndDate()))
                    .findFirst()
                    .ifPresent(expense::setBudget);
        }

        expense = expenseRepository.save(expense);

        // Mettre à jour le budget si associé
        if (expense.getBudget() != null) {
            budgetService.updateBudgetSpent(expense.getBudget().getId());
        }

        return convertToDto(expense);
    }

    public List<ExpenseDto> getUserExpenses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return expenseRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ExpenseDto updateExpense(Long userId, Long expenseId, ExpenseDto expenseDto) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        expense.setDescription(expenseDto.getDescription());
        expense.setCategory(expenseDto.getCategory());
        expense.setAmount(expenseDto.getAmount());
        expense.setDate(expenseDto.getDate());
        expense.setPaymentMethod(expenseDto.getPaymentMethod());

        expense = expenseRepository.save(expense);
        return convertToDto(expense);
    }

    public void deleteExpense(Long userId, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        expenseRepository.delete(expense);
    }

    private ExpenseDto convertToDto(Expense expense) {
        ExpenseDto dto = new ExpenseDto();
        dto.setId(expense.getId());
        dto.setDescription(expense.getDescription());
        dto.setCategory(expense.getCategory());
        dto.setAmount(expense.getAmount());
        dto.setDate(expense.getDate());
        dto.setPaymentMethod(expense.getPaymentMethod());
        return dto;
    }
}

