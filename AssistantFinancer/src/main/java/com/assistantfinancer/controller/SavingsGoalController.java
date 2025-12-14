package com.assistantfinancer.controller;

import com.assistantfinancer.dto.SavingsGoalDto;
import com.assistantfinancer.model.SavingsGoal;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.SavingsGoalRepository;
import com.assistantfinancer.repository.UserRepository;
import com.assistantfinancer.service.GamificationService;
import com.assistantfinancer.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private GamificationService gamificationService;

    @PostMapping
    public ResponseEntity<SavingsGoalDto> createGoal(@RequestBody SavingsGoalDto goalDto) {
        Long userId = userUtil.getUserIdFromAuth();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        SavingsGoal goal = new SavingsGoal();
        goal.setName(goalDto.getName());
        goal.setDescription(goalDto.getDescription());
        goal.setTargetAmount(goalDto.getTargetAmount());
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setTargetDate(goalDto.getTargetDate());
        goal.setCompleted(false);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal.setUser(user);

        goal = savingsGoalRepository.save(goal);
        
        // Ajouter des points pour la création d'un objectif
        gamificationService.addPoints(userId, 10);
        
        return ResponseEntity.ok(convertToDto(goal));
    }

    @GetMapping
    public ResponseEntity<List<SavingsGoalDto>> getUserGoals() {
        Long userId = userUtil.getUserIdFromAuth();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<SavingsGoal> goals = savingsGoalRepository.findByUser(user);
        return ResponseEntity.ok(goals.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
    }

    @PutMapping("/{id}/add")
    public ResponseEntity<SavingsGoalDto> addToGoal(@PathVariable Long id, @RequestBody BigDecimal amount) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        
        Long userId = userUtil.getUserIdFromAuth();
        if (!goal.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
        goal.setUpdatedAt(LocalDateTime.now());

        // Vérifier si l'objectif est atteint
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCompleted(true);
            gamificationService.addPoints(userId, 50);
        }

        goal = savingsGoalRepository.save(goal);
        return ResponseEntity.ok(convertToDto(goal));
    }

    private SavingsGoalDto convertToDto(SavingsGoal goal) {
        SavingsGoalDto dto = new SavingsGoalDto();
        dto.setId(goal.getId());
        dto.setName(goal.getName());
        dto.setDescription(goal.getDescription());
        dto.setTargetAmount(goal.getTargetAmount());
        dto.setCurrentAmount(goal.getCurrentAmount());
        dto.setRemainingAmount(goal.getTargetAmount().subtract(goal.getCurrentAmount()));
        
        double progress = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
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

