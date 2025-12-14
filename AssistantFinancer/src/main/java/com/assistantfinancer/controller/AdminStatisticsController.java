package com.assistantfinancer.controller;

import com.assistantfinancer.dto.DashboardStatisticsDto;
import com.assistantfinancer.model.UserProgress;
import com.assistantfinancer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatisticsDto> getDashboardStatistics() {
        DashboardStatisticsDto stats = new DashboardStatisticsDto();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalBudgets(budgetRepository.count());
        stats.setTotalExpenses(expenseRepository.count());
        stats.setTotalCourses(courseRepository.count());
        stats.setCompletedCourses(userProgressRepository.findAll().stream()
                .filter(UserProgress::isCompleted)
                .count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/user-evolution")
    public ResponseEntity<List<Map<String, Object>>> getUserEvolution() {
        // Retourner l'évolution des utilisateurs par date
        // Pour simplifier, on retourne les 30 derniers jours
        List<Map<String, Object>> evolution = new ArrayList<>();
        long totalUsers = userRepository.count();

        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            // Approximation : on divise le total par 30 pour simuler une croissance
            dayData.put("count", (totalUsers * (30 - i)) / 30);
            evolution.add(dayData);
        }

        return ResponseEntity.ok(evolution);
    }

    @GetMapping("/budget-categories")
    public ResponseEntity<List<Map<String, Object>>> getBudgetCategoryDistribution() {
        Map<String, Long> categoryCount = budgetRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        budget -> budget.getCategory() != null ? budget.getCategory() : "OTHER",
                        Collectors.counting()
                ));

        List<Map<String, Object>> distribution = categoryCount.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("category", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/top-users")
    public ResponseEntity<List<Map<String, Object>>> getTopUsers(@RequestParam(defaultValue = "5") int limit) {
        List<Map<String, Object>> topUsers = userProfileRepository.findAll().stream()
                .filter(profile -> profile.getPoints() != null && profile.getPoints() > 0)
                .sorted((p1, p2) -> Integer.compare(p2.getPoints(), p1.getPoints()))
                .limit(limit)
                .map(profile -> {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("username", profile.getUser().getUsername());
                    userData.put("points", profile.getPoints());
                    return userData;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(topUsers);
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Map<String, Object>>> getCourseStatistics() {
        List<Map<String, Object>> courseStats = courseRepository.findAll().stream()
                .map(course -> {
                    List<UserProgress> progressList = userProgressRepository.findAll().stream()
                            .filter(up -> up.getCourse().getId().equals(course.getId()))
                            .collect(Collectors.toList());

                    long completions = progressList.stream()
                            .filter(UserProgress::isCompleted)
                            .count();

                    double averageScore = progressList.stream()
                            .filter(up -> up.getScore() != null)
                            .mapToInt(UserProgress::getScore)
                            .average()
                            .orElse(0.0);

                    Map<String, Object> stats = new HashMap<>();
                    stats.put("courseId", course.getId());
                    stats.put("courseTitle", course.getTitle());
                    stats.put("completions", completions);
                    stats.put("averageScore", averageScore);
                    return stats;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(courseStats);
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportData(@RequestParam String format) {
        // Pour l'instant, on retourne juste un message
        // Vous pouvez implémenter l'export CSV/PDF ici
        return ResponseEntity.ok("Export " + format + " - À implémenter");
    }
}

