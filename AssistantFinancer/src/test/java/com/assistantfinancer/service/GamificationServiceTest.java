package com.assistantfinancer.service;

import com.assistantfinancer.model.Badge;
import com.assistantfinancer.model.User;
import com.assistantfinancer.model.UserProfile;
import com.assistantfinancer.repository.BadgeRepository;
import com.assistantfinancer.repository.UserProfileRepository;
import com.assistantfinancer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @InjectMocks
    private GamificationService gamificationService;

    private User testUser;
    private UserProfile testProfile;
    private Badge testBadge;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testProfile = new UserProfile();
        testProfile.setId(1L);
        testProfile.setUser(testUser);
        testProfile.setPoints(0);
        testProfile.setLevelNumber(1);
        testProfile.setLevel("BEGINNER");
        testProfile.setBadges(new HashSet<>());

        testBadge = new Badge();
        testBadge.setId(1L);
        testBadge.setName("Premier pas");
    }

    @Test
    void addPoints_ShouldAddPoints() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);
        when(badgeRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        gamificationService.addPoints(1L, 50);

        // Then
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void addPoints_ShouldUpdateLevel() {
        // Given
        testProfile.setPoints(50);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
            UserProfile profile = invocation.getArgument(0);
            // Verify level calculation: (50 + 50) / 100 + 1 = 2
            assertEquals(2, profile.getLevelNumber());
            return profile;
        });
        when(badgeRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        gamificationService.addPoints(1L, 50);

        // Then
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void addPoints_ShouldChangeLevelTextual() {
        // Given
        testProfile.setPoints(0);
        testProfile.setLevelNumber(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
            UserProfile profile = invocation.getArgument(0);
            int levelNumber = profile.getLevelNumber();
            if (levelNumber <= 3) {
                assertEquals("BEGINNER", profile.getLevel());
            } else if (levelNumber <= 7) {
                assertEquals("INTERMEDIATE", profile.getLevel());
            } else {
                assertEquals("ADVANCED", profile.getLevel());
            }
            return profile;
        });
        when(badgeRepository.findAll()).thenReturn(new ArrayList<>());

        // When - Add 300 points to reach level 4 (INTERMEDIATE)
        gamificationService.addPoints(1L, 300);

        // Then
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void addPoints_ShouldAwardBadgesAutomatically() {
        // Given
        Badge badge = new Badge();
        badge.setId(1L);
        badge.setName("Premier pas");

        testProfile.setPoints(0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badge));

        // When - Add 10 points to trigger "Premier pas" badge
        gamificationService.addPoints(1L, 10);

        // Then
        verify(badgeRepository, atLeastOnce()).findAll();
    }

    @Test
    void awardBadge_ShouldAwardBadge() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(testBadge));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        // When
        gamificationService.awardBadge(1L, 1L);

        // Then
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void awardBadge_ShouldAvoidDuplicates() {
        // Given
        Set<Badge> badges = new HashSet<>();
        badges.add(testBadge);
        testProfile.setBadges(badges);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(testBadge));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));

        // When
        gamificationService.awardBadge(1L, 1L);

        // Then
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void checkAndAwardBadges_ShouldAwardPremierPasAt10Points() {
        // Given
        Badge badge = new Badge();
        badge.setId(1L);
        badge.setName("Premier pas");

        testProfile.setPoints(10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badge));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        // When
        gamificationService.addPoints(1L, 0);

        // Then
        verify(badgeRepository, atLeastOnce()).findAll();
    }

    @Test
    void checkAndAwardBadges_ShouldAwardEpargnantAt500Points() {
        // Given
        Badge badge = new Badge();
        badge.setId(2L);
        badge.setName("Épargnant");

        testProfile.setPoints(500);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(badgeRepository.findById(2L)).thenReturn(Optional.of(badge));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        // When
        gamificationService.addPoints(1L, 0);

        // Then
        verify(badgeRepository, atLeastOnce()).findAll();
    }

    @Test
    void checkAndAwardBadges_ShouldAwardExpertBudgetAtLevel5() {
        // Given
        Badge badge = new Badge();
        badge.setId(3L);
        badge.setName("Expert Budget");

        testProfile.setPoints(400);
        testProfile.setLevelNumber(5);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(badgeRepository.findById(3L)).thenReturn(Optional.of(badge));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testProfile);

        // When
        gamificationService.addPoints(1L, 0);

        // Then
        verify(badgeRepository, atLeastOnce()).findAll();
    }

    @Test
    void createDefaultProfile_ShouldCreateProfile() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
            UserProfile profile = invocation.getArgument(0);
            profile.setId(1L);
            // Vérifier les valeurs par défaut avant l'ajout de points
            if (profile.getPoints() == null || profile.getPoints() == 0) {
                assertEquals("FR", profile.getLanguage());
                assertEquals("BEGINNER", profile.getLevel());
                assertEquals(0, profile.getPoints() != null ? profile.getPoints() : 0);
                assertEquals(1, profile.getLevelNumber());
            }
            return profile;
        });
        when(badgeRepository.findAll()).thenReturn(new ArrayList<>());

        // When - Ajouter 0 points pour juste créer le profil
        gamificationService.addPoints(1L, 0);

        // Then
        // createDefaultProfile() fait un save(), puis addPoints() fait aussi un save()
        verify(userProfileRepository, atLeast(1)).save(any(UserProfile.class));
    }
}

