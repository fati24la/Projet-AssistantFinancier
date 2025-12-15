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
    private BudgetRepository budgetRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public DashboardDto getDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));

        // Calculer les revenus et dépenses sur le dernier mois
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        LocalDate now = LocalDate.now();

        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, oneMonthAgo, now);

        // Épargne totale = somme des montants courants de TOUS les objectifs d'épargne
        List<SavingsGoal> allGoals = savingsGoalRepository.findByUser(user);
        BigDecimal totalSavings = allGoals.stream()
                .map(goal -> goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Dépenses totales (inchangé)
        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Somme des montants restants sur les budgets actifs
        List<Budget> budgets = budgetRepository.findByUser(user);
        BigDecimal totalBudgetRemaining = budgets.stream()
                .map(b -> {
                    BigDecimal amount = b.getAmount() != null ? b.getAmount() : BigDecimal.ZERO;
                    BigDecimal spent = b.getSpent() != null ? b.getSpent() : BigDecimal.ZERO;
                    return amount.subtract(spent).max(BigDecimal.ZERO);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Revenus = somme des budgets restants + épargne totale (logique métier simplifiée)
        BigDecimal totalIncome = totalBudgetRemaining.add(totalSavings);

        // Dette totale
        BigDecimal totalDebt = profile.getTotalDebt() != null 
                ? profile.getTotalDebt()
                : BigDecimal.ZERO;

        // Score de santé financière (0-100)
        BigDecimal financialHealthScore = calculateFinancialHealthScore(
                totalIncome, totalExpenses, totalSavings, totalDebt);

        // Données d'évolution sur 1 mois (granularité journalière)
        List<DashboardDto.MonthlyData> monthlyData = calculateDailyData(user, oneMonthAgo, now, profile.getMonthlyIncome());

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

    /**
     * Calcule les données d'évolution sur 1 mois avec une granularité journalière.
     * On continue à utiliser DashboardDto.MonthlyData pour rester compatible avec le front.
     */
    private List<DashboardDto.MonthlyData> calculateDailyData(User user, LocalDate start, LocalDate end, BigDecimal monthlyIncome) {
        List<DashboardDto.MonthlyData> dailyData = new ArrayList<>();
        LocalDate current = start;

        BigDecimal safeMonthlyIncome = monthlyIncome != null ? monthlyIncome : BigDecimal.ZERO;

        while (!current.isAfter(end)) {
            // Revenus journaliers approximés à partir du revenu mensuel
            int daysInMonth = current.lengthOfMonth();
            BigDecimal dailyIncome = daysInMonth > 0
                    ? safeMonthlyIncome.divide(BigDecimal.valueOf(daysInMonth), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Dépenses du jour courant
            List<Expense> dayExpenses = expenseRepository.findByUserAndDateBetween(user, current, current);
            BigDecimal dayExpensesTotal = dayExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            dailyData.add(new DashboardDto.MonthlyData(
                    current.format(DateTimeFormatter.ofPattern("dd/MM")),
                    dailyIncome,
                    dayExpensesTotal
            ));

            current = current.plusDays(1);
        }

        return dailyData;
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

