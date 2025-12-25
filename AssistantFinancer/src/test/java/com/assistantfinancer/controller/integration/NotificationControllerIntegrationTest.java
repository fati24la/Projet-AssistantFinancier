package com.assistantfinancer.controller.integration;

import com.assistantfinancer.model.Notification;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.NotificationRepository;
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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        notificationRepository.deleteAll();

        testUser = testHelper.createTestUser("testuser", "test@example.com", "password123");
        authToken = testHelper.generateTokenForUser(testUser);
    }

    @Test
    void testCreateNotification_ShouldCreateSuccessfully() throws Exception {
        // Les notifications sont généralement créées par le système, mais testons la récupération
        Notification notification = new Notification();
        notification.setTitle("Test Notification");
        notification.setMessage("This is a test notification");
        notification.setType("BUDGET_ALERT");
        notification.setRead(false);
        notification.setUser(testUser);
        notification.setCreatedAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        assertNotNull(notification.getId());
    }

    @Test
    void testGetUserNotifications_ShouldReturnOnlyUserNotifications() throws Exception {
        // Créer plusieurs notifications
        Notification notification1 = new Notification();
        notification1.setTitle("Notification 1");
        notification1.setMessage("First notification");
        notification1.setType("BUDGET_ALERT");
        notification1.setRead(false);
        notification1.setUser(testUser);
        notification1.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification1);

        Notification notification2 = new Notification();
        notification2.setTitle("Notification 2");
        notification2.setMessage("Second notification");
        notification2.setType("GOAL_REACHED");
        notification2.setRead(true);
        notification2.setUser(testUser);
        notification2.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification2);

        // Créer une notification pour un autre utilisateur
        User otherUser = testHelper.createTestUser("otheruser", "other@example.com", "password123");
        Notification otherNotification = new Notification();
        otherNotification.setTitle("Other User Notification");
        otherNotification.setMessage("This should not appear");
        otherNotification.setType("BUDGET_ALERT");
        otherNotification.setRead(false);
        otherNotification.setUser(otherUser);
        otherNotification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(otherNotification);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].message").exists());
    }

    @Test
    void testGetUnreadNotifications_ShouldReturnOnlyUnread() throws Exception {
        Notification readNotification = new Notification();
        readNotification.setTitle("Read Notification");
        readNotification.setMessage("This is read");
        readNotification.setType("BUDGET_ALERT");
        readNotification.setRead(true);
        readNotification.setUser(testUser);
        readNotification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(readNotification);

        Notification unreadNotification1 = new Notification();
        unreadNotification1.setTitle("Unread Notification 1");
        unreadNotification1.setMessage("This is unread");
        unreadNotification1.setType("GOAL_REACHED");
        unreadNotification1.setRead(false);
        unreadNotification1.setUser(testUser);
        unreadNotification1.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(unreadNotification1);

        Notification unreadNotification2 = new Notification();
        unreadNotification2.setTitle("Unread Notification 2");
        unreadNotification2.setMessage("This is also unread");
        unreadNotification2.setType("EDUCATIONAL_TIP");
        unreadNotification2.setRead(false);
        unreadNotification2.setUser(testUser);
        unreadNotification2.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(unreadNotification2);

        mockMvc.perform(get("/api/notifications/unread")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].read").value(false))
                .andExpect(jsonPath("$[1].read").value(false));
    }

    @Test
    void testMarkAsRead_ShouldMarkNotificationAsRead() throws Exception {
        Notification notification = new Notification();
        notification.setTitle("Test Notification");
        notification.setMessage("This is a test");
        notification.setType("BUDGET_ALERT");
        notification.setRead(false);
        notification.setUser(testUser);
        notification.setCreatedAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        assertFalse(notification.isRead());

        mockMvc.perform(put("/api/notifications/" + notification.getId() + "/read")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk());

        // Vérifier en base
        Notification updated = notificationRepository.findById(notification.getId()).orElse(null);
        assertNotNull(updated);
        assertTrue(updated.isRead());
    }

    @Test
    void testMarkAllAsRead_ShouldMarkAllNotificationsAsRead() throws Exception {
        Notification notification1 = new Notification();
        notification1.setTitle("Notification 1");
        notification1.setMessage("First notification");
        notification1.setType("BUDGET_ALERT");
        notification1.setRead(false);
        notification1.setUser(testUser);
        notification1.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification1);

        Notification notification2 = new Notification();
        notification2.setTitle("Notification 2");
        notification2.setMessage("Second notification");
        notification2.setType("GOAL_REACHED");
        notification2.setRead(false);
        notification2.setUser(testUser);
        notification2.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification2);

        mockMvc.perform(put("/api/notifications/read-all")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk());

        // Vérifier que toutes les notifications sont marquées comme lues
        notificationRepository.findByUser(testUser).forEach(notif -> {
            assertTrue(notif.isRead());
        });
    }

    @Test
    void testDeleteNotification_ShouldDeleteSuccessfully() throws Exception {
        Notification notification = new Notification();
        notification.setTitle("Notification to Delete");
        notification.setMessage("This will be deleted");
        notification.setType("BUDGET_ALERT");
        notification.setRead(false);
        notification.setUser(testUser);
        notification.setCreatedAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        mockMvc.perform(delete("/api/notifications/" + notification.getId())
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(status().isOk());

        // Vérifier que la notification a été supprimée
        assertFalse(notificationRepository.findById(notification.getId()).isPresent());
    }

    @Test
    void testGetNotifications_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testMarkAsRead_UnauthorizedAccess_ShouldReturnError() throws Exception {
        User otherUser = testHelper.createTestUser("otheruser", "other@example.com", "password123");
        
        Notification notification = new Notification();
        notification.setTitle("Other User Notification");
        notification.setMessage("This belongs to another user");
        notification.setType("BUDGET_ALERT");
        notification.setRead(false);
        notification.setUser(otherUser);
        notification.setCreatedAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        // Tenter de marquer comme lue une notification d'un autre utilisateur
        // Le service devrait gérer l'autorisation
        mockMvc.perform(put("/api/notifications/" + notification.getId() + "/read")
                        .header("Authorization", testHelper.getAuthHeader(authToken)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Le service lance RuntimeException: Unauthorized qui devient 500
                    assertTrue(status >= 400 && status < 600, 
                        () -> "Expected error status but got: " + status);
                });
    }
}

