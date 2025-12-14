package com.assistantfinancer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal totalSavings;
    private BigDecimal totalDebt;
    private BigDecimal financialHealthScore;
    private List<MonthlyData> monthlyData;
    private List<CategoryExpense> categoryExpenses;
    private List<SavingsGoalDto> activeGoals;
    private Integer totalPoints;
    private Integer level;
    private Integer unreadNotifications;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyData {
        private String month;
        private BigDecimal income;
        private BigDecimal expenses;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryExpense {
        private String category;
        private BigDecimal amount;
        private Double percentage;
    }
}

