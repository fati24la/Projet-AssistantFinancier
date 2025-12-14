package com.assistantfinancer.service;

import com.assistantfinancer.dto.DashboardDto;
import com.assistantfinancer.model.*;
import com.assistantfinancer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public DashboardDto getDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));

        // Calculer les revenus et dépenses des 6 derniers mois
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        LocalDate now = LocalDate.now();

        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, sixMonthsAgo, now);
        
        // Revenus simulés (basés sur le profil utilisateur)
        BigDecimal totalIncome = profile.getMonthlyIncome() != null 
                ? profile.getMonthlyIncome().multiply(BigDecimal.valueOf(6))
                : BigDecimal.ZERO;

        // Dépenses totales
        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Épargne totale
        BigDecimal totalSavings = profile.getTotalSavings() != null 
                ? profile.getTotalSavings()
                : BigDecimal.ZERO;

        // Dette totale
        BigDecimal totalDebt = profile.getTotalDebt() != null 
                ? profile.getTotalDebt()
                : BigDecimal.ZERO;

        // Score de santé financière (0-100)
        BigDecimal financialHealthScore = calculateFinancialHealthScore(
                totalIncome, totalExpenses, totalSavings, totalDebt);

        // Données mensuelles
        List<DashboardDto.MonthlyData> monthlyData = calculateMonthlyData(user, sixMonthsAgo, now, profile.getMonthlyIncome());

        // Dépenses par catégorie
        List<DashboardDto.CategoryExpense> categoryExpenses = calculateCategoryExpenses(expenses, totalExpenses);

        // Objectifs d'épargne actifs
        List<SavingsGoal> activeGoals = savingsGoalRepository.findByUserAndCompleted(user, false);
        List<com.assistantfinancer.dto.SavingsGoalDto> goalDtos = activeGoals.stream()
                .map(this::convertToSavingsGoalDto)
                .collect(Collectors.toList());

        // Notifications non lues
        int unreadNotifications = notificationRepository.findByUserAndIsRead(user, false).size();

        DashboardDto dashboard = new DashboardDto();
        dashboard.setTotalIncome(totalIncome);
        dashboard.setTotalExpenses(totalExpenses);
        dashboard.setTotalSavings(totalSavings);
        dashboard.setTotalDebt(totalDebt);
        dashboard.setFinancialHealthScore(financialHealthScore);
        dashboard.setMonthlyData(monthlyData);
        dashboard.setCategoryExpenses(categoryExpenses);
        dashboard.setActiveGoals(goalDtos);
        dashboard.setTotalPoints(profile.getPoints() != null ? profile.getPoints() : 0);
        dashboard.setLevel(profile.getLevelNumber() != null ? profile.getLevelNumber() : 1);
        dashboard.setUnreadNotifications(unreadNotifications);

        return dashboard;
    }

    private UserProfile createDefaultProfile(User user) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setLanguage("FR");
        profile.setLevel("BEGINNER");
        profile.setPoints(0);
        profile.setLevelNumber(1);
        profile.setMonthlyIncome(BigDecimal.ZERO);
        profile.setMonthlyExpenses(BigDecimal.ZERO);
        profile.setTotalSavings(BigDecimal.ZERO);
        profile.setTotalDebt(BigDecimal.ZERO);
        profile.setCreatedAt(java.time.LocalDateTime.now());
        profile.setUpdatedAt(java.time.LocalDateTime.now());
        return userProfileRepository.save(profile);
    }

    private BigDecimal calculateFinancialHealthScore(BigDecimal income, BigDecimal expenses, 
                                                     BigDecimal savings, BigDecimal debt) {
        if (income.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(50); // Score par défaut
        }

        // Ratio épargne/revenu (40% du score)
        BigDecimal savingsRatio = income.compareTo(BigDecimal.ZERO) > 0
                ? savings.divide(income, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal savingsScore = savingsRatio.multiply(BigDecimal.valueOf(40))
                .min(BigDecimal.valueOf(40));

        // Ratio dépenses/revenu (30% du score)
        BigDecimal expenseRatio = income.compareTo(BigDecimal.ZERO) > 0
                ? expenses.divide(income, 4, RoundingMode.HALF_UP)
                : BigDecimal.ONE;
        BigDecimal expenseScore = BigDecimal.valueOf(30)
                .subtract(expenseRatio.multiply(BigDecimal.valueOf(30)))
                .max(BigDecimal.ZERO);

        // Ratio dette/revenu (30% du score)
        BigDecimal debtRatio = income.compareTo(BigDecimal.ZERO) > 0
                ? debt.divide(income, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal debtScore = BigDecimal.valueOf(30)
                .subtract(debtRatio.multiply(BigDecimal.valueOf(30)))
                .max(BigDecimal.ZERO);

        return savingsScore.add(expenseScore).add(debtScore);
    }

    private List<DashboardDto.MonthlyData> calculateMonthlyData(User user, LocalDate start, LocalDate end, BigDecimal monthlyIncome) {
        List<DashboardDto.MonthlyData> monthlyData = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            LocalDate monthStart = current.withDayOfMonth(1);
            LocalDate monthEnd = current.withDayOfMonth(current.lengthOfMonth());

            List<Expense> monthExpenses = expenseRepository.findByUserAndDateBetween(user, monthStart, monthEnd);
            BigDecimal monthExpensesTotal = monthExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthlyData.add(new DashboardDto.MonthlyData(
                    current.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    monthlyIncome != null ? monthlyIncome : BigDecimal.ZERO,
                    monthExpensesTotal
            ));

            current = current.plusMonths(1);
        }

        return monthlyData;
    }

    private List<DashboardDto.CategoryExpense> calculateCategoryExpenses(List<Expense> expenses, BigDecimal totalExpenses) {
        Map<String, BigDecimal> categoryMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        return categoryMap.entrySet().stream()
                .map(entry -> {
                    Double percentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                            ? entry.getValue().divide(totalExpenses, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)).doubleValue()
                            : 0.0;
                    return new DashboardDto.CategoryExpense(entry.getKey(), entry.getValue(), percentage);
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());
    }

    private com.assistantfinancer.dto.SavingsGoalDto convertToSavingsGoalDto(SavingsGoal goal) {
        com.assistantfinancer.dto.SavingsGoalDto dto = new com.assistantfinancer.dto.SavingsGoalDto();
        dto.setId(goal.getId());
        dto.setName(goal.getName());
        dto.setDescription(goal.getDescription());
        dto.setTargetAmount(goal.getTargetAmount());
        dto.setCurrentAmount(goal.getCurrentAmount());
        dto.setRemainingAmount(goal.getTargetAmount().subtract(goal.getCurrentAmount()));
        
        Double progress = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                ? goal.getCurrentAmount().divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;
        dto.setProgressPercentage(progress);
        
        dto.setTargetDate(goal.getTargetDate());
        dto.setDaysRemaining(ChronoUnit.DAYS.between(LocalDate.now(), goal.getTargetDate()));
        dto.setCompleted(goal.isCompleted());
        
        return dto;
    }
}

