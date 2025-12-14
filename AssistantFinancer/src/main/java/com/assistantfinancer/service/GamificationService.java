package com.assistantfinancer.service;

import com.assistantfinancer.model.Badge;
import com.assistantfinancer.model.User;
import com.assistantfinancer.model.UserProfile;
import com.assistantfinancer.repository.BadgeRepository;
import com.assistantfinancer.repository.UserProfileRepository;
import com.assistantfinancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class GamificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    public void addPoints(Long userId, Integer points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));

        Integer currentPoints = profile.getPoints() != null ? profile.getPoints() : 0;
        profile.setPoints(currentPoints + points);

        // Mettre à jour le niveau (100 points par niveau)
        Integer newLevel = (currentPoints + points) / 100 + 1;
        profile.setLevelNumber(newLevel);

        // Déterminer le niveau textuel
        if (newLevel <= 3) {
            profile.setLevel("BEGINNER");
        } else if (newLevel <= 7) {
            profile.setLevel("INTERMEDIATE");
        } else {
            profile.setLevel("ADVANCED");
        }

        userProfileRepository.save(profile);
        checkAndAwardBadges(userId);
    }

    public void awardBadge(Long userId, Long badgeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new RuntimeException("Badge not found"));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));

        Set<Badge> badges = profile.getBadges();
        if (!badges.contains(badge)) {
            badges.add(badge);
            profile.setBadges(badges);
            userProfileRepository.save(profile);
        }
    }

    private void checkAndAwardBadges(Long userId) {
        UserProfile profile = userProfileRepository.findByUser(
                userRepository.findById(userId).orElseThrow()
        ).orElse(null);

        if (profile == null) return;

        // Badge "Premier pas" - 10 points
        if (profile.getPoints() >= 10) {
            awardBadgeIfNotExists(userId, "Premier pas");
        }

        // Badge "Épargnant" - 500 points
        if (profile.getPoints() >= 500) {
            awardBadgeIfNotExists(userId, "Épargnant");
        }

        // Badge "Expert Budget" - niveau 5
        if (profile.getLevelNumber() >= 5) {
            awardBadgeIfNotExists(userId, "Expert Budget");
        }
    }

    private void awardBadgeIfNotExists(Long userId, String badgeName) {
        Badge badge = badgeRepository.findAll().stream()
                .filter(b -> b.getName().equals(badgeName))
                .findFirst()
                .orElse(null);

        if (badge != null) {
            awardBadge(userId, badge.getId());
        }
    }

    private UserProfile createDefaultProfile(User user) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setLanguage("FR");
        profile.setLevel("BEGINNER");
        profile.setPoints(0);
        profile.setLevelNumber(1);
        profile.setCreatedAt(java.time.LocalDateTime.now());
        profile.setUpdatedAt(java.time.LocalDateTime.now());
        return userProfileRepository.save(profile);
    }
}

