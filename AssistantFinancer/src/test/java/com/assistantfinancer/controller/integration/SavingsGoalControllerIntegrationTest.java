package com.assistantfinancer.controller.integration;

import com.assistantfinancer.dto.SavingsGoalDto;
import com.assistantfinancer.model.SavingsGoal;
import com.assistantfinancer.model.User;
import com.assistantfinancer.model.UserProfile;
import com.assistantfinancer.repository.SavingsGoalRepository;
import com.assistantfinancer.repository.UserProfileRepository;
import com.assistantfinancer.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class SavingsGoalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

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
    void testCreateSavingsGoal_ShouldCreateAndAwardPoints() throws Exception {
        SavingsGoalDto goalDto = new SavingsGoalDto();
        goalDto.setName("Test Goal");
        goalDto.setDescription("Save for vacation");
        goalDto.setTargetAmount(new BigDecimal("5000.00"));
        goalDto.setTargetDate(LocalDate.now().plusMonths(6));

        String response = mockMvc.perform(post("/api/savings-goals")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Goal"))
                .andExpect(jsonPath("$.targetAmount").value(5000.00))
                .andExpect(jsonPath("$.currentAmount").value(0))
                .andExpect(jsonPath("$.completed").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Vérifier que l'objectif a été créé
        SavingsGoal savedGoal = savingsGoalRepository.findByUser(testUser).stream()
                .findFirst()
                .orElse(null);
        assertNotNull(savedGoal);
        assertEquals("Test Goal", savedGoal.getName());

        // Vérifier que des points ont été attribués (10 points pour la création)
        UserProfile profile = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profile);
        assertEquals(10, profile.getPoints());
    }

    @Test
    void testGetUserGoals_ShouldReturnOnlyUserGoals() throws Exception {
        SavingsGoal goal1 = new SavingsGoal();
        goal1.setName("Goal 1");
        goal1.setDescription("First goal");
        goal1.setTargetAmount(new BigDecimal("1000.00"));
        goal1.setCurrentAmount(BigDecimal.ZERO);
        goal1.setTargetDate(LocalDate.now().plusMonths(3));
        goal1.setCompleted(false);
        goal1.setUser(testUser);
        goal1.setCreatedAt(LocalDateTime.now());
        goal1.setUpdatedAt(LocalDateTime.now());
        savingsGoalRepository.save(goal1);

        SavingsGoal goal2 = new SavingsGoal();
        goal2.setName("Goal 2");
        goal2.setDescription("Second goal");
        goal2.setTargetAmount(new BigDecimal("2000.00"));
        goal2.setCurrentAmount(BigDecimal.ZERO);
        goal2.setTargetDate(LocalDate.now().plusMonths(6));
        goal2.setCompleted(false);
        goal2.setUser(testUser);
        goal2.setCreatedAt(LocalDateTime.now());
        goal2.setUpdatedAt(LocalDateTime.now());
        savingsGoalRepository.save(goal2);

        mockMvc.perform(get("/api/savings-goals")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testAddToGoal_ShouldUpdateCurrentAmount() throws Exception {
        SavingsGoal goal = new SavingsGoal();
        goal.setName("Test Goal");
        goal.setDescription("Save for vacation");
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setCompleted(false);
        goal.setUser(testUser);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal = savingsGoalRepository.save(goal);

        BigDecimal amountToAdd = new BigDecimal("1000.00");

        mockMvc.perform(put("/api/savings-goals/" + goal.getId() + "/add")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amountToAdd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(1000.00))
                .andExpect(jsonPath("$.completed").value(false));

        // Vérifier en base
        SavingsGoal updated = savingsGoalRepository.findById(goal.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(new BigDecimal("1000.00"), updated.getCurrentAmount());
    }

    @Test
    void testAddToGoal_CompleteGoal_ShouldMarkAsCompletedAndAwardPoints() throws Exception {
        SavingsGoal goal = new SavingsGoal();
        goal.setName("Test Goal");
        goal.setDescription("Save for vacation");
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setCurrentAmount(new BigDecimal("4000.00"));
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setCompleted(false);
        goal.setUser(testUser);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal = savingsGoalRepository.save(goal);

        // Obtenir les points initiaux
        UserProfile profileBefore = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profileBefore);
        int pointsBefore = profileBefore.getPoints() != null ? profileBefore.getPoints() : 0;

        BigDecimal amountToAdd = new BigDecimal("1000.00");

        mockMvc.perform(put("/api/savings-goals/" + goal.getId() + "/add")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amountToAdd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(5000.00))
                .andExpect(jsonPath("$.completed").value(true));

        // Vérifier que des points ont été attribués (50 points pour la complétion)
        UserProfile profileAfter = userProfileRepository.findByUser(testUser).orElse(null);
        assertNotNull(profileAfter);
        assertEquals(pointsBefore + 50, profileAfter.getPoints());

        // Vérifier en base
        SavingsGoal updated = savingsGoalRepository.findById(goal.getId()).orElse(null);
        assertNotNull(updated);
        assertTrue(updated.isCompleted());
        assertEquals(new BigDecimal("5000.00"), updated.getCurrentAmount());
    }

    @Test
    void testAddToGoal_ExceedTarget_ShouldStillComplete() throws Exception {
        SavingsGoal goal = new SavingsGoal();
        goal.setName("Test Goal");
        goal.setDescription("Save for vacation");
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setCurrentAmount(new BigDecimal("4000.00"));
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setCompleted(false);
        goal.setUser(testUser);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal = savingsGoalRepository.save(goal);

        BigDecimal amountToAdd = new BigDecimal("2000.00"); // Plus que nécessaire

        mockMvc.perform(put("/api/savings-goals/" + goal.getId() + "/add")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amountToAdd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(6000.00))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void testAddToGoal_UnauthorizedAccess_ShouldReturnError() throws Exception {
        User otherUser = testHelper.createTestUser("otheruser", "other@example.com", "password123");
        
        SavingsGoal goal = new SavingsGoal();
        goal.setName("Other User Goal");
        goal.setDescription("Other user's goal");
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setCompleted(false);
        goal.setUser(otherUser);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal = savingsGoalRepository.save(goal);

        BigDecimal amountToAdd = new BigDecimal("1000.00");

        // Tenter d'ajouter à l'objectif d'un autre utilisateur
        mockMvc.perform(put("/api/savings-goals/" + goal.getId() + "/add")
                        .header("Authorization", testHelper.getAuthHeader(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amountToAdd)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Le service lance RuntimeException: Unauthorized qui devient 500
                    assertTrue(status >= 400 && status < 600, 
                        () -> "Expected error status but got: " + status);
                });
    }

    @Test
    void testCreateSavingsGoal_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        SavingsGoalDto goalDto = new SavingsGoalDto();
        goalDto.setName("Test Goal");
        goalDto.setTargetAmount(new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/savings-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalDto)))
                .andExpect(status().isForbidden());
    }
}

