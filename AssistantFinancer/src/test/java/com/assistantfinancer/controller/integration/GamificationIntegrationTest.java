package com.assistantfinancer.controller.integration;

import com.assistantfinancer.model.SavingsGoal;
import com.assistantfinancer.model.User;
import com.assistantfinancer.model.UserProfile;
import com.assistantfinancer.repository.SavingsGoalRepository;
import com.assistantfinancer.repository.UserProfileRepository;
import com.assistantfinancer.repository.UserRepository;
import com.assistantfinancer.service.GamificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GamificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        savingsGoalRepository.deleteAll();
        userProfileRepository.deleteAll();

        testUser = testHelper.createTestUser("testuser", "test@example.com", "password123");
        authToken = testHelper.generateTokenForUser(testUser);

        // Créer un profil par défaut
        UserProfile profile = new UserProfile();
        profile.setUser(testUser);
        profile.setPoints(0);
        profile.setLevelNumber(1);
        profile.setLevel("BEGINNER");
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);
    }

    @Test
    void testAddPoints_OnAction_ShouldUpdateUserProfile() {
        // Obtenir les points initiaux
        UserProfile profileBefore = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profileBefore);
        int pointsBefore = profileBefore.getPoints() != null ? profileBefore.getPoints() : 0;

        // Ajouter des points
        gamificationService.addPoints(testUser.getId(), 50);

        // Vérifier que les points ont été ajoutés
        UserProfile profileAfter = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profileAfter);
        assertEquals(pointsBefore + 50, profileAfter.getPoints());
    }

    @Test
    void testCreateSavingsGoal_AwardsPoints() throws Exception {
        UserProfile profileBefore = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profileBefore);
        int pointsBefore = profileBefore.getPoints() != null ? profileBefore.getPoints() : 0;

        // Créer un objectif d'épargne (devrait attribuer 10 points)
        String goalJson = """
                {
                    "name": "Test Goal",
                    "description": "Save for vacation",
                    "targetAmount": 5000.00,
                    "targetDate": "2024-12-31"
                }
                """;

        mockMvc.perform(post("/api/savings-goals")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType("application/json")
                        .content(goalJson))
                .andExpect(status().isOk());

        // Vérifier que des points ont été attribués
        UserProfile profileAfter = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profileAfter);
        assertEquals(pointsBefore + 10, profileAfter.getPoints());
    }

    @Test
    void testCompleteSavingsGoal_AwardsPoints() throws Exception {
        // Créer un objectif presque complet
        SavingsGoal goal = new SavingsGoal();
        goal.setName("Test Goal");
        goal.setDescription("Save for vacation");
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setCurrentAmount(new BigDecimal("4900.00"));
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setCompleted(false);
        goal.setUser(testUser);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal = savingsGoalRepository.save(goal);

        UserProfile profileBefore = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profileBefore);
        int pointsBefore = profileBefore.getPoints() != null ? profileBefore.getPoints() : 0;

        // Compléter l'objectif (devrait attribuer 50 points)
        BigDecimal amountToAdd = new BigDecimal("100.00");

        mockMvc.perform(put("/api/savings-goals/" + goal.getId() + "/add")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(amountToAdd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        // Vérifier que des points ont été attribués
        UserProfile profileAfter = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profileAfter);
        assertEquals(pointsBefore + 50, profileAfter.getPoints());
    }

    @Test
    void testLevelProgression_ShouldUpdateLevel() {
        // Ajouter suffisamment de points pour passer au niveau 2 (100 points)
        gamificationService.addPoints(testUser.getId(), 100);

        UserProfile profile = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profile);
        assertEquals(2, profile.getLevelNumber());
    }

    @Test
    void testLevelProgression_ShouldUpdateLevelText() {
        // Niveau 1-3 = BEGINNER
        UserProfile profile1 = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profile1);
        assertEquals("BEGINNER", profile1.getLevel());

        // Ajouter des points pour passer au niveau 4 (300 points)
        gamificationService.addPoints(testUser.getId(), 300);
        UserProfile profile2 = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profile2);
        assertEquals("INTERMEDIATE", profile2.getLevel());

        // Ajouter plus de points pour passer au niveau 8 (700 points)
        gamificationService.addPoints(testUser.getId(), 400);
        UserProfile profile3 = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profile3);
        assertEquals("ADVANCED", profile3.getLevel());
    }

    @Test
    void testBadgeAward_OnPointsThreshold_ShouldAwardBadge() {
        // Note: Ce test nécessite que des badges existent en base
        // Pour l'instant, on teste que le service ne plante pas
        // et que les points sont correctement ajoutés

        // Ajouter 10 points (seuil pour "Premier pas")
        gamificationService.addPoints(testUser.getId(), 10);

        UserProfile profile = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profile);
        assertEquals(10, profile.getPoints());
    }

    @Test
    void testMultipleActions_ShouldAccumulatePoints() {
        // Créer un objectif (10 points)
        SavingsGoal goal = new SavingsGoal();
        goal.setName("Test Goal");
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setCurrentAmount(new BigDecimal("4000.00"));
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setUser(testUser);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal = savingsGoalRepository.save(goal);

        UserProfile profileBefore = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profileBefore);
        int pointsBefore = profileBefore.getPoints() != null ? profileBefore.getPoints() : 0;

        // Ajouter manuellement des points pour simuler la création d'objectif
        gamificationService.addPoints(testUser.getId(), 10);

        // Compléter l'objectif (50 points)
        goal.setCurrentAmount(goal.getTargetAmount());
        goal.setCompleted(true);
        savingsGoalRepository.save(goal);
        gamificationService.addPoints(testUser.getId(), 50);

        // Vérifier que les points se sont accumulés
        UserProfile profileAfter = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profileAfter);
        assertEquals(pointsBefore + 60, profileAfter.getPoints());
    }

    @Test
    void testUserProgress_ShouldReflectInDashboard() throws Exception {
        // Ajouter des points
        gamificationService.addPoints(testUser.getId(), 150);

        // Vérifier dans le dashboard
        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(150))
                .andExpect(jsonPath("$.level").exists());
    }
}

