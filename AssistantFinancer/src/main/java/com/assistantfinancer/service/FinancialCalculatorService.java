package com.assistantfinancer.service;

import com.assistantfinancer.dto.CalculatorRequest;
import com.assistantfinancer.dto.CalculatorResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class FinancialCalculatorService {

    public CalculatorResponse calculate(CalculatorRequest request) {
        CalculatorResponse response = new CalculatorResponse();
        response.setCalculatorType(request.getCalculatorType());
        
        Map<String, Object> results = new HashMap<>();
        String explanation = "";

        switch (request.getCalculatorType()) {
            case "CREDIT":
                results = calculateCredit(request);
                explanation = "Calcul des mensualités de crédit avec intérêts composés.";
                break;
            case "SAVINGS":
                results = calculateSavings(request);
                explanation = "Calcul du temps nécessaire pour atteindre un objectif d'épargne.";
                break;
            case "INVESTMENT":
                results = calculateInvestment(request);
                explanation = "Calcul de la rentabilité d'un investissement.";
                break;
            case "BORROWING_CAPACITY":
                results = calculateBorrowingCapacity(request);
                explanation = "Calcul de la capacité d'emprunt basée sur les revenus et dépenses.";
                break;
            default:
                throw new RuntimeException("Calculator type not supported");
        }

        response.setResults(results);
        response.setExplanation(explanation);
        return response;
    }

    private Map<String, Object> calculateCredit(CalculatorRequest request) {
        Map<String, Object> results = new HashMap<>();
        
        BigDecimal principal = request.getPrincipal();
        BigDecimal annualRate = request.getInterestRate();
        Integer months = request.getDurationMonths();

        if (principal == null || annualRate == null || months == null) {
            throw new RuntimeException("Missing required parameters for credit calculation");
        }

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);

        // Formule: M = P * [r(1+r)^n] / [(1+r)^n - 1]
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowerN = onePlusR.pow(months);
        BigDecimal numerator = monthlyRate.multiply(onePlusRPowerN);
        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);
        
        BigDecimal monthlyPayment = principal.multiply(numerator)
                .divide(denominator, 2, RoundingMode.HALF_UP);

        BigDecimal totalAmount = monthlyPayment.multiply(BigDecimal.valueOf(months));
        BigDecimal totalInterest = totalAmount.subtract(principal);

        results.put("monthlyPayment", monthlyPayment);
        results.put("totalAmount", totalAmount);
        results.put("totalInterest", totalInterest);
        results.put("principal", principal);
        results.put("durationMonths", months);

        return results;
    }

    private Map<String, Object> calculateSavings(CalculatorRequest request) {
        Map<String, Object> results = new HashMap<>();
        
        BigDecimal targetAmount = request.getTargetAmount();
        BigDecimal monthlyContribution = request.getMonthlyContribution();
        BigDecimal currentSavings = request.getCurrentSavings() != null 
                ? request.getCurrentSavings() 
                : BigDecimal.ZERO;
        BigDecimal annualRate = request.getInterestRate() != null 
                ? request.getInterestRate() 
                : BigDecimal.ZERO;

        if (targetAmount == null || monthlyContribution == null) {
            throw new RuntimeException("Missing required parameters for savings calculation");
        }

        BigDecimal remaining = targetAmount.subtract(currentSavings);
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);

        // Calcul simplifié (sans intérêts composés mensuels pour simplifier)
        Integer months;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            months = remaining.divide(monthlyContribution, 0, RoundingMode.UP).intValue();
        } else {
            // Formule avec intérêts: n = log((FV*r + PMT) / (PV*r + PMT)) / log(1+r)
            // Simplification pour l'exemple
            months = remaining.divide(monthlyContribution, 0, RoundingMode.UP).intValue();
        }

        BigDecimal totalContribution = monthlyContribution.multiply(BigDecimal.valueOf(months));
        BigDecimal estimatedInterest = totalContribution.subtract(remaining);

        results.put("monthsNeeded", months);
        results.put("totalContribution", totalContribution);
        results.put("estimatedInterest", estimatedInterest);
        results.put("targetAmount", targetAmount);
        results.put("currentSavings", currentSavings);

        return results;
    }

    private Map<String, Object> calculateInvestment(CalculatorRequest request) {
        Map<String, Object> results = new HashMap<>();
        
        BigDecimal principal = request.getPrincipal();
        BigDecimal annualRate = request.getExpectedReturn();
        Integer years = request.getDurationMonths() != null 
                ? request.getDurationMonths() / 12 
                : 1;

        if (principal == null || annualRate == null) {
            throw new RuntimeException("Missing required parameters for investment calculation");
        }

        BigDecimal rate = annualRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal futureValue = principal.multiply(
                BigDecimal.ONE.add(rate).pow(years)
        ).setScale(2, RoundingMode.HALF_UP);

        BigDecimal profit = futureValue.subtract(principal);

        results.put("initialInvestment", principal);
        results.put("futureValue", futureValue);
        results.put("profit", profit);
        results.put("returnPercentage", annualRate);
        results.put("years", years);

        return results;
    }

    private Map<String, Object> calculateBorrowingCapacity(CalculatorRequest request) {
        Map<String, Object> results = new HashMap<>();
        
        BigDecimal monthlyIncome = request.getMonthlyIncome();
        BigDecimal monthlyExpenses = request.getMonthlyExpenses();
        BigDecimal otherDebts = request.getOtherDebts() != null 
                ? request.getOtherDebts() 
                : BigDecimal.ZERO;

        if (monthlyIncome == null || monthlyExpenses == null) {
            throw new RuntimeException("Missing required parameters for borrowing capacity calculation");
        }

        // Capacité de remboursement = 33% du revenu net
        BigDecimal netIncome = monthlyIncome.subtract(monthlyExpenses).subtract(otherDebts);
        BigDecimal maxMonthlyPayment = netIncome.multiply(BigDecimal.valueOf(0.33))
                .setScale(2, RoundingMode.HALF_UP);

        // Estimation du montant empruntable (taux 5%, 20 ans)
        BigDecimal annualRate = BigDecimal.valueOf(5);
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        Integer months = 20 * 12; // 20 ans

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowerN = onePlusR.pow(months);
        BigDecimal numerator = monthlyRate.multiply(onePlusRPowerN);
        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);
        
        BigDecimal borrowingCapacity = maxMonthlyPayment
                .divide(numerator.divide(denominator, 6, RoundingMode.HALF_UP), 2, RoundingMode.HALF_UP);

        results.put("monthlyIncome", monthlyIncome);
        results.put("monthlyExpenses", monthlyExpenses);
        results.put("netIncome", netIncome);
        results.put("maxMonthlyPayment", maxMonthlyPayment);
        results.put("borrowingCapacity", borrowingCapacity);
        results.put("estimatedRate", annualRate);
        results.put("estimatedDuration", months);

        return results;
    }
}

