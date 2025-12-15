package com.assistantfinancer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialProfileUpdateDto {
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal totalSavings;
    private BigDecimal totalDebt;
}


