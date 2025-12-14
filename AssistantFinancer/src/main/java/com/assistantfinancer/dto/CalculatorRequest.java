package com.assistantfinancer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculatorRequest {
    private String calculatorType; // CREDIT, SAVINGS, INVESTMENT, BUDGET, LOAN_COMPARISON, BORROWING_CAPACITY
    private BigDecimal principal;
    private BigDecimal interestRate;
    private Integer durationMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal targetAmount;
    private BigDecimal monthlyContribution;
    private BigDecimal currentSavings;
    private BigDecimal expectedReturn;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal otherDebts;
}

