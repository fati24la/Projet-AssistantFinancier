package com.assistantfinancer.service;

import com.assistantfinancer.model.Notification;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.NotificationRepository;
import com.assistantfinancer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setTitle("Test Notification");
        testNotification.setMessage("Test message");
        testNotification.setType("BUDGET_ALERT");
        testNotification.setUser(testUser);
        testNotification.setRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
        testNotification.setScheduledFor(LocalDateTime.now());
    }

    @Test
    void createNotification_ShouldCreateNotification() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            return notification;
        });

        // When
        Notification result = notificationService.createNotification(1L, "Test", "Message", "BUDGET_ALERT");

        // Then
        assertNotNull(result);
        assertEquals("Test", result.getTitle());
        assertEquals("Message", result.getMessage());
        assertEquals("BUDGET_ALERT", result.getType());
        assertFalse(result.isRead());
        verify(userRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void getUserNotifications_ShouldReturnNotifications() {
        // Given
        List<Notification> notifications = List.of(testNotification);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByUser(testUser)).thenReturn(notifications);

        // When
        List<Notification> result = notificationService.getUserNotifications(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Notification", result.get(0).getTitle());
        verify(userRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).findByUser(testUser);
    }

    @Test
    void getUnreadNotifications_ShouldReturnUnreadNotifications() {
        // Given
        List<Notification> unreadNotifications = List.of(testNotification);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(unreadNotifications);

        // When
        List<Notification> result = notificationService.getUnreadNotifications(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
        verify(userRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).findByUserAndIsRead(testUser, false);
    }

    @Test
    void markAsRead_ShouldMarkAsRead() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.markAsRead(1L, 1L);

        // Then
        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void markAsRead_WithUnauthorizedUser_ShouldThrowException() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testNotification.setUser(otherUser);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.markAsRead(1L, 1L);
        });

        assertEquals("Unauthorized", exception.getMessage());
        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAllAsRead_ShouldMarkAllAsRead() {
        // Given
        List<Notification> unreadNotifications = new ArrayList<>();
        unreadNotifications.add(testNotification);
        
        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setRead(false);
        notification2.setUser(testUser);
        unreadNotifications.add(notification2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByUserAndIsRead(testUser, false)).thenReturn(unreadNotifications);
        when(notificationRepository.saveAll(any())).thenReturn(unreadNotifications);

        // When
        notificationService.markAllAsRead(1L);

        // Then
        verify(userRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).findByUserAndIsRead(testUser, false);
        verify(notificationRepository, times(1)).saveAll(any());
    }

    @Test
    void deleteNotification_ShouldDeleteNotification() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        doNothing().when(notificationRepository).delete(testNotification);

        // When
        notificationService.deleteNotification(1L, 1L);

        // Then
        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).delete(testNotification);
    }

    @Test
    void deleteNotification_WithUnauthorizedUser_ShouldThrowException() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testNotification.setUser(otherUser);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.deleteNotification(1L, 1L);
        });

        assertEquals("Unauthorized", exception.getMessage());
        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, never()).delete(any(Notification.class));
    }
}

