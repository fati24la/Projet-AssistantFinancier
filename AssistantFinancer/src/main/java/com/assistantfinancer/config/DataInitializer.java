package com.assistantfinancer.config;

import com.assistantfinancer.model.*;
import com.assistantfinancer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

// Désactivé - Les données doivent être ajoutées manuellement
// @Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Vérifier si des données existent déjà
        if (userRepository.count() > 0) {
            System.out.println("✅ Des données existent déjà. Ignorons l'initialisation.");
            return;
        }

        System.out.println("🚀 Initialisation des données de test...");

        // 1. Créer un utilisateur de test
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);
        System.out.println("✅ Utilisateur créé: " + testUser.getUsername());

        // 2. Créer le profil utilisateur
        UserProfile profile = new UserProfile();
        profile.setUser(testUser);
        profile.setLanguage("FR");
        profile.setLevel("INTERMEDIATE");
        profile.setPoints(350);
        profile.setLevelNumber(4);
        profile.setMonthlyIncome(new BigDecimal("5000.00"));
        profile.setMonthlyExpenses(new BigDecimal("3500.00"));
        profile.setTotalSavings(new BigDecimal("15000.00"));
        profile.setTotalDebt(new BigDecimal("5000.00"));
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        profile = userProfileRepository.save(profile);
        System.out.println("✅ Profil utilisateur créé");

        // 3. Créer des budgets
        Budget budget1 = createBudget(testUser, "Budget Alimentation", "ALIMENTATION", 
                new BigDecimal("1500.00"), new BigDecimal("1200.00"));
        Budget budget2 = createBudget(testUser, "Budget Transport", "TRANSPORT", 
                new BigDecimal("800.00"), new BigDecimal("650.00"));
        Budget budget3 = createBudget(testUser, "Budget Santé", "SANTE", 
                new BigDecimal("500.00"), new BigDecimal("200.00"));
        System.out.println("✅ Budgets créés");

        // 4. Créer des dépenses
        createExpense(testUser, budget1, "Courses supermarché", "ALIMENTATION", 
                new BigDecimal("450.00"), LocalDate.now().minusDays(5));
        createExpense(testUser, budget1, "Restaurant", "ALIMENTATION", 
                new BigDecimal("150.00"), LocalDate.now().minusDays(2));
        createExpense(testUser, budget2, "Essence", "TRANSPORT", 
                new BigDecimal("300.00"), LocalDate.now().minusDays(3));
        System.out.println("✅ Dépenses créées");

        // 5. Créer des objectifs d'épargne
        SavingsGoal goal1 = new SavingsGoal();
        goal1.setUser(testUser);
        goal1.setName("Achat voiture");
        goal1.setDescription("Économiser pour acheter une voiture d'occasion");
        goal1.setTargetAmount(new BigDecimal("50000.00"));
        goal1.setCurrentAmount(new BigDecimal("15000.00"));
        goal1.setTargetDate(LocalDate.now().plusMonths(18));
        goal1.setCompleted(false);
        goal1.setCreatedAt(LocalDateTime.now());
        goal1.setUpdatedAt(LocalDateTime.now());
        savingsGoalRepository.save(goal1);

        SavingsGoal goal2 = new SavingsGoal();
        goal2.setUser(testUser);
        goal2.setName("Voyage en Europe");
        goal2.setDescription("Budget pour un voyage de 2 semaines");
        goal2.setTargetAmount(new BigDecimal("20000.00"));
        goal2.setCurrentAmount(new BigDecimal("5000.00"));
        goal2.setTargetDate(LocalDate.now().plusMonths(7));
        goal2.setCompleted(false);
        goal2.setCreatedAt(LocalDateTime.now());
        goal2.setUpdatedAt(LocalDateTime.now());
        savingsGoalRepository.save(goal2);
        System.out.println("✅ Objectifs d'épargne créés");

        // 6. Créer des badges
        Badge badge1 = new Badge();
        badge1.setName("Premier pas");
        badge1.setDescription("Vous avez commencé votre parcours financier");
        badge1.setIcon("🎯");
        badge1.setCategory("EDUCATION");
        badge1.setRequirement("{\"points\": 10}");
        badgeRepository.save(badge1);

        Badge badge2 = new Badge();
        badge2.setName("Épargnant");
        badge2.setDescription("Vous avez économisé régulièrement");
        badge2.setIcon("💰");
        badge2.setCategory("SAVINGS");
        badge2.setRequirement("{\"points\": 500}");
        badgeRepository.save(badge2);
        System.out.println("✅ Badges créés");

        // 7. Créer des cours
        Course course1 = createCourse("Introduction à la gestion budgétaire",
                "Apprenez les bases de la gestion budgétaire personnelle",
                "BUDGETING", "BEGINNER", 30, "FR");
        Course course2 = createCourse("Les bases de l'épargne",
                "Découvrez comment épargner efficacement",
                "SAVINGS", "BEGINNER", 25, "FR");
        System.out.println("✅ Cours créés");

        // 8. Créer des notifications
        Notification notif1 = new Notification();
        notif1.setUser(testUser);
        notif1.setTitle("Budget Alimentation");
        notif1.setMessage("Vous avez dépensé 80% de votre budget alimentation ce mois");
        notif1.setType("BUDGET_ALERT");
        notif1.setRead(false);
        notif1.setCreatedAt(LocalDateTime.now());
        notif1.setScheduledFor(LocalDateTime.now());
        notificationRepository.save(notif1);
        System.out.println("✅ Notifications créées");

        System.out.println("🎉 Initialisation terminée avec succès !");
        System.out.println("📝 Connectez-vous avec : username='testuser', password='password123'");
    }

    private Budget createBudget(User user, String name, String category, 
                                BigDecimal amount, BigDecimal spent) {
        Budget budget = new Budget();
        budget.setUser(user);
        budget.setName(name);
        budget.setCategory(category);
        budget.setAmount(amount);
        budget.setSpent(spent);
        budget.setStartDate(LocalDate.now().withDayOfMonth(1));
        budget.setEndDate(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        budget.setPeriod("MONTHLY");
        budget.setCreatedAt(LocalDateTime.now());
        return budgetRepository.save(budget);
    }

    private Expense createExpense(User user, Budget budget, String description, 
                                 String category, BigDecimal amount, LocalDate date) {
        Expense expense = new Expense();
        expense.setUser(user);
        expense.setBudget(budget);
        expense.setDescription(description);
        expense.setCategory(category);
        expense.setAmount(amount);
        expense.setDate(date);
        expense.setPaymentMethod("CARD");
        expense.setCreatedAt(LocalDateTime.now());
        return expenseRepository.save(expense);
    }

    private Course createCourse(String title, String description, String category, 
                               String difficulty, Integer duration, String language) {
        Course course = new Course();
        course.setTitle(title);
        course.setDescription(description);
        course.setContent("Contenu du cours " + title + "...");
        course.setCategory(category);
        course.setDifficulty(difficulty);
        course.setDurationMinutes(duration);
        course.setLanguage(language);
        course.setActive(true);
        course.setCreatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }
}

