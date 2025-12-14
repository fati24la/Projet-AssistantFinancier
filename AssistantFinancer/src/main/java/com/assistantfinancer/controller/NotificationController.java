package com.assistantfinancer.controller;

import com.assistantfinancer.model.Notification;
import com.assistantfinancer.service.NotificationService;
import com.assistantfinancer.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserUtil userUtil;

    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications() {
        Long userId = userUtil.getUserIdFromAuth();
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications() {
        Long userId = userUtil.getUserIdFromAuth();
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Long userId = userUtil.getUserIdFromAuth();
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Long userId = userUtil.getUserIdFromAuth();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        Long userId = userUtil.getUserIdFromAuth();
        notificationService.deleteNotification(userId, id);
        return ResponseEntity.ok().build();
    }
}

