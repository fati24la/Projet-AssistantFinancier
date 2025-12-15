package com.assistantfinancer.controller;

import com.assistantfinancer.dto.AdminUserDto;
import com.assistantfinancer.dto.AdminUserDetailsDto;
import com.assistantfinancer.dto.BudgetDto;
import com.assistantfinancer.dto.ExpenseDto;
import com.assistantfinancer.dto.SavingsGoalDto;
import com.assistantfinancer.dto.UserProgressDto;
import com.assistantfinancer.dto.CourseDto;
import com.assistantfinancer.model.*;
import com.assistantfinancer.repository.*;
import com.assistantfinancer.repository.QuestionRepository;
import com.assistantfinancer.repository.ResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<Page<AdminUserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size);
        Page<User> users;

        if (search != null && !search.isEmpty()) {
            // Recherche simple par username ou email
            List<User> allUsers = userRepository.findAll();
            List<User> filtered = allUsers.stream()
                    .filter(user -> user.getUsername().toLowerCase().contains(search.toLowerCase()) ||
                            user.getEmail().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
            
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filtered.size());
            List<User> pageContent = filtered.subList(start, end);
            users = new PageImpl<>(pageContent, pageable, filtered.size());
        } else {
            users = userRepository.findAll(pageable);
        }

        Page<AdminUserDto> userDtos = users.map(user -> {
            AdminUserDto dto = new AdminUserDto();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setEnabled(user.getEnabled() != null ? user.getEnabled() : true); // Utiliser le champ enabled du User
            dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now());

            // Récupérer le profil pour les points
            userProfileRepository.findByUser(user).ifPresent(profile -> {
                dto.setTotalPoints(profile.getPoints());
            });

            // Compter les budgets, dépenses et cours complétés
            dto.setBudgetsCount((long) budgetRepository.findByUser(user).size());
            dto.setExpensesCount((long) expenseRepository.findByUser(user).size());
            dto.setCompletedCoursesCount((long) userProgressRepository.findByUserAndCompleted(user, true).size());

            return dto;
        });

        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDetailsDto> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AdminUserDetailsDto dto = new AdminUserDetailsDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.getEnabled() != null ? user.getEnabled() : true);
        dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : LocalDateTime.now().toString());

        // Récupérer le profil
        userProfileRepository.findByUser(user).ifPresent(profile -> {
            dto.setTotalPoints(profile.getPoints());
        });

        // Récupérer les budgets
        List<BudgetDto> budgets = budgetRepository.findByUser(user).stream()
                .map(budget -> {
                    BudgetDto bdto = new BudgetDto();
                    bdto.setId(budget.getId());
                    bdto.setName(budget.getName());
                    bdto.setCategory(budget.getCategory());
                    bdto.setAmount(budget.getAmount());
                    bdto.setSpent(budget.getSpent());
                    bdto.setRemaining(budget.getAmount().subtract(budget.getSpent()));
                    bdto.setPercentageUsed(budget.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0 
                        ? budget.getSpent().divide(budget.getAmount(), 2, java.math.RoundingMode.HALF_UP).multiply(java.math.BigDecimal.valueOf(100)).doubleValue()
                        : 0.0);
                    bdto.setStartDate(budget.getStartDate());
                    bdto.setEndDate(budget.getEndDate());
                    bdto.setPeriod(budget.getPeriod());
                    return bdto;
                })
                .collect(Collectors.toList());
        dto.setBudgets(budgets);

        // Récupérer les dépenses
        List<ExpenseDto> expenses = expenseRepository.findByUser(user).stream()
                .map(expense -> {
                    ExpenseDto edto = new ExpenseDto();
                    edto.setId(expense.getId());
                    edto.setDescription(expense.getDescription());
                    edto.setCategory(expense.getCategory());
                    edto.setAmount(expense.getAmount());
                    edto.setDate(expense.getDate());
                    edto.setPaymentMethod(expense.getPaymentMethod());
                    return edto;
                })
                .collect(Collectors.toList());
        dto.setExpenses(expenses);

        // Récupérer la progression
        List<UserProgressDto> progress = userProgressRepository.findByUser(user).stream()
                .map(up -> {
                    UserProgressDto pdto = new UserProgressDto();
                    pdto.setId(up.getId());
                    pdto.setCompleted(up.isCompleted());
                    pdto.setScore(up.getScore());
                    pdto.setStartedAt(up.getStartedAt() != null ? up.getStartedAt().toString() : null);
                    pdto.setCompletedAt(up.getCompletedAt() != null ? up.getCompletedAt().toString() : null);
                    
                    CourseDto cdto = new CourseDto();
                    cdto.setId(up.getCourse().getId());
                    cdto.setTitle(up.getCourse().getTitle());
                    pdto.setCourse(cdto);
                    
                    return pdto;
                })
                .collect(Collectors.toList());
        dto.setProgress(progress);

        // Récupérer les objectifs d'épargne
        List<SavingsGoalDto> goals = savingsGoalRepository.findByUser(user).stream()
                .map(goal -> {
                    SavingsGoalDto gdto = new SavingsGoalDto();
                    gdto.setId(goal.getId());
                    gdto.setName(goal.getName());
                    gdto.setDescription(goal.getDescription());
                    gdto.setTargetAmount(goal.getTargetAmount());
                    gdto.setCurrentAmount(goal.getCurrentAmount());
                    gdto.setRemainingAmount(goal.getTargetAmount().subtract(goal.getCurrentAmount()));
                    gdto.setProgressPercentage(goal.getTargetAmount().compareTo(java.math.BigDecimal.ZERO) > 0
                        ? goal.getCurrentAmount().divide(goal.getTargetAmount(), 2, java.math.RoundingMode.HALF_UP).multiply(java.math.BigDecimal.valueOf(100)).doubleValue()
                        : 0.0);
                    gdto.setTargetDate(goal.getTargetDate());
                    gdto.setDaysRemaining(goal.getTargetDate() != null 
                        ? java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), goal.getTargetDate())
                        : null);
                    gdto.setCompleted(goal.isCompleted());
                    return gdto;
                })
                .collect(Collectors.toList());
        dto.setSavingsGoals(goals);

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        try {
            System.out.println("🔄 [AdminUserController] Toggle status pour l'utilisateur ID: " + id);
            
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                System.out.println("❌ [AdminUserController] Utilisateur non trouvé: " + id);
                return ResponseEntity.status(404).body(Map.of("message", "User not found with id: " + id));
            }
            
            // Toggle le statut enabled
            boolean currentStatus = user.getEnabled() != null && user.getEnabled();
            boolean newStatus = !currentStatus;
            user.setEnabled(newStatus);
            userRepository.save(user);
            
            System.out.println("✅ [AdminUserController] Statut de l'utilisateur " + user.getUsername() + " changé de " + currentStatus + " à " + newStatus);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("❌ [AdminUserController] Erreur lors du toggle status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Error toggling user status", "error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            System.out.println("🗑️ [AdminUserController] Tentative de suppression de l'utilisateur ID: " + id);
            
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                System.out.println("❌ [AdminUserController] Utilisateur non trouvé: " + id);
                return ResponseEntity.status(404).body(Map.of("message", "User not found with id: " + id));
            }
            
            System.out.println("✅ [AdminUserController] Utilisateur trouvé: " + user.getUsername());
            
            // Supprimer d'abord les données liées pour éviter les erreurs de contrainte
            // Supprimer les questions et réponses liées
            try {
                List<Question> questions = questionRepository.findAll().stream()
                        .filter(q -> q.getUser() != null && q.getUser().getId().equals(id))
                        .collect(Collectors.toList());
                System.out.println("📝 Suppression de " + questions.size() + " questions");
                for (Question question : questions) {
                    // Supprimer les réponses liées à cette question
                    List<Response> responses = responseRepository.findAll().stream()
                            .filter(r -> r.getQuestion() != null && r.getQuestion().getId().equals(question.getId()))
                            .collect(Collectors.toList());
                    System.out.println("💬 Suppression de " + responses.size() + " réponses pour la question " + question.getId());
                    responseRepository.deleteAll(responses);
                    questionRepository.delete(question);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Erreur lors de la suppression des questions/réponses: " + e.getMessage());
            }
            
            // Supprimer les budgets
            try {
                List<Budget> budgets = budgetRepository.findByUser(user);
                System.out.println("📊 Suppression de " + budgets.size() + " budgets");
                budgetRepository.deleteAll(budgets);
            } catch (Exception e) {
                System.out.println("⚠️ Erreur lors de la suppression des budgets: " + e.getMessage());
            }
            
            // Supprimer les dépenses
            try {
                List<Expense> expenses = expenseRepository.findByUser(user);
                System.out.println("💰 Suppression de " + expenses.size() + " dépenses");
                expenseRepository.deleteAll(expenses);
            } catch (Exception e) {
                System.out.println("⚠️ Erreur lors de la suppression des dépenses: " + e.getMessage());
            }
            
            // Supprimer la progression
            try {
                List<UserProgress> progress = userProgressRepository.findByUser(user);
                System.out.println("📚 Suppression de " + progress.size() + " progressions");
                userProgressRepository.deleteAll(progress);
            } catch (Exception e) {
                System.out.println("⚠️ Erreur lors de la suppression des progressions: " + e.getMessage());
            }
            
            // Supprimer les objectifs d'épargne
            try {
                List<SavingsGoal> goals = savingsGoalRepository.findByUser(user);
                System.out.println("🎯 Suppression de " + goals.size() + " objectifs d'épargne");
                savingsGoalRepository.deleteAll(goals);
            } catch (Exception e) {
                System.out.println("⚠️ Erreur lors de la suppression des objectifs: " + e.getMessage());
            }
            
            // Supprimer les notifications
            try {
                List<Notification> notifications = notificationRepository.findByUser(user);
                System.out.println("🔔 Suppression de " + notifications.size() + " notifications");
                notificationRepository.deleteAll(notifications);
            } catch (Exception e) {
                System.out.println("⚠️ Erreur lors de la suppression des notifications: " + e.getMessage());
            }
            
            // Supprimer le profil utilisateur
            try {
                userProfileRepository.findByUser(user).ifPresent(profile -> {
                    System.out.println("👤 Suppression du profil utilisateur");
                    userProfileRepository.delete(profile);
                });
            } catch (Exception e) {
                System.out.println("⚠️ Erreur lors de la suppression du profil: " + e.getMessage());
            }
            
            // Détacher l'utilisateur de la session Hibernate pour éviter les problèmes de cascade
            // Réinitialiser les collections pour éviter ConcurrentModificationException
            System.out.println("🗑️ Suppression finale de l'utilisateur: " + user.getUsername());
            user.setBudgets(null);
            user.setExpenses(null);
            user.setSavingsGoals(null);
            user.setProgress(null);
            user.setNotifications(null);
            
            // Forcer le flush pour s'assurer que toutes les suppressions précédentes sont effectuées
            userRepository.flush();
            
            // Maintenant supprimer l'utilisateur
            userRepository.delete(user);
            userRepository.flush();
            
            System.out.println("✅ [AdminUserController] Utilisateur supprimé avec succès");
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            System.err.println("❌ [AdminUserController] RuntimeException: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
            return ResponseEntity.status(404).body(Map.of("message", "User not found", "error", errorMsg));
        } catch (Exception e) {
            System.err.println("❌ [AdminUserController] Exception: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
            return ResponseEntity.status(500).body(Map.of("message", "Error deleting user", "error", errorMsg));
        }
    }
}

