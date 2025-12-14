package com.assistantfinancer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailsDto {
    private Long id;
    private String username;
    private String email;
    private String createdAt;
    private Boolean enabled;
    private Integer totalPoints;
    private List<BudgetDto> budgets;
    private List<ExpenseDto> expenses;
    private List<UserProgressDto> progress;
    private List<SavingsGoalDto> savingsGoals;
}

