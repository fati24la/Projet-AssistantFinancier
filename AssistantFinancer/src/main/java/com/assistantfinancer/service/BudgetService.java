package com.assistantfinancer.service;

import com.assistantfinancer.dto.BudgetDto;
import com.assistantfinancer.model.Budget;
import com.assistantfinancer.model.Expense;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.BudgetRepository;
import com.assistantfinancer.repository.ExpenseRepository;
import com.assistantfinancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    public BudgetDto createBudget(Long userId, BudgetDto budgetDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = new Budget();
        budget.setName(budgetDto.getName());
        budget.setCategory(budgetDto.getCategory());
        budget.setAmount(budgetDto.getAmount());
        budget.setSpent(BigDecimal.ZERO);
        budget.setStartDate(budgetDto.getStartDate());
        budget.setEndDate(budgetDto.getEndDate());
        budget.setPeriod(budgetDto.getPeriod());
        budget.setUser(user);
        budget.setCreatedAt(LocalDateTime.now());

        budget = budgetRepository.save(budget);
        return convertToDto(budget);
    }

    public List<BudgetDto> getUserBudgets(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Budget> budgets = budgetRepository.findByUser(user);
        return budgets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BudgetDto updateBudget(Long userId, Long budgetId, BudgetDto budgetDto) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (!budget.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        budget.setName(budgetDto.getName());
        budget.setCategory(budgetDto.getCategory());
        budget.setAmount(budgetDto.getAmount());
        budget.setStartDate(budgetDto.getStartDate());
        budget.setEndDate(budgetDto.getEndDate());
        budget.setPeriod(budgetDto.getPeriod());

        budget = budgetRepository.save(budget);
        return convertToDto(budget);
    }

    public void deleteBudget(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (!budget.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        budgetRepository.delete(budget);
    }

    private BudgetDto convertToDto(Budget budget) {
        BudgetDto dto = new BudgetDto();
        dto.setId(budget.getId());
        dto.setName(budget.getName());
        dto.setCategory(budget.getCategory());
        dto.setAmount(budget.getAmount());
        dto.setSpent(budget.getSpent());
        dto.setRemaining(budget.getAmount().subtract(budget.getSpent()));
        
        Double percentage = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? budget.getSpent().divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;
        dto.setPercentageUsed(percentage);
        
        dto.setStartDate(budget.getStartDate());
        dto.setEndDate(budget.getEndDate());
        dto.setPeriod(budget.getPeriod());
        
        return dto;
    }

    public void updateBudgetSpent(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(
                budget.getUser(),
                budget.getStartDate(),
                budget.getEndDate()
        );

        BigDecimal spent = expenses.stream()
                .filter(e -> budget.getCategory().equals(e.getCategory()))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        budget.setSpent(spent);
        budgetRepository.save(budget);
    }
}

