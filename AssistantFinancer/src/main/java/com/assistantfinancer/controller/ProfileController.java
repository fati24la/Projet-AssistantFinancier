package com.assistantfinancer.controller;

import com.assistantfinancer.dto.FinancialProfileUpdateDto;
import com.assistantfinancer.model.User;
import com.assistantfinancer.model.UserProfile;
import com.assistantfinancer.repository.UserProfileRepository;
import com.assistantfinancer.repository.UserRepository;
import com.assistantfinancer.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserUtil userUtil;

    @PutMapping("/financial")
    public ResponseEntity<UserProfile> updateFinancialProfile(
            @RequestBody FinancialProfileUpdateDto request) {
        Long userId = userUtil.getUserIdFromAuth();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));

        if (request.getMonthlyIncome() != null) {
            profile.setMonthlyIncome(request.getMonthlyIncome());
        }
        if (request.getMonthlyExpenses() != null) {
            profile.setMonthlyExpenses(request.getMonthlyExpenses());
        }
        if (request.getTotalSavings() != null) {
            profile.setTotalSavings(request.getTotalSavings());
        }
        if (request.getTotalDebt() != null) {
            profile.setTotalDebt(request.getTotalDebt());
        }
        profile.setUpdatedAt(LocalDateTime.now());

        UserProfile saved = userProfileRepository.save(profile);
        return ResponseEntity.ok(saved);
    }

    private UserProfile createDefaultProfile(User user) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setLanguage("FR");
        profile.setLevel("BEGINNER");
        profile.setPoints(0);
        profile.setLevelNumber(1);
        profile.setMonthlyIncome(java.math.BigDecimal.ZERO);
        profile.setMonthlyExpenses(java.math.BigDecimal.ZERO);
        profile.setTotalSavings(java.math.BigDecimal.ZERO);
        profile.setTotalDebt(java.math.BigDecimal.ZERO);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        return userProfileRepository.save(profile);
    }
}


