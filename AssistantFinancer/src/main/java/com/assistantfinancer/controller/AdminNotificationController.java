package com.assistantfinancer.controller;

import com.assistantfinancer.dto.NotificationRequest;
import com.assistantfinancer.model.Notification;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.NotificationRepository;
import com.assistantfinancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, Object>> broadcastNotification(@RequestBody NotificationRequest request) {
        List<User> allUsers = userRepository.findAll();
        int recipientsCount = 0;

        for (User user : allUsers) {
            Notification notification = new Notification();
            notification.setTitle(request.getTitle());
            notification.setMessage(request.getMessage());
            notification.setType("SYSTEM");
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUser(user);
            notificationRepository.save(notification);
            recipientsCount++;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Notification envoyée avec succès");
        response.put("recipientsCount", recipientsCount);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getNotificationHistory() {
        // Récupérer les notifications système (type SYSTEM)
        List<Notification> systemNotifications = notificationRepository.findAll().stream()
                .filter(n -> "SYSTEM".equals(n.getType()))
                .collect(Collectors.toList());

        // Grouper par titre et message pour créer l'historique
        Map<String, Map<String, Object>> grouped = new HashMap<>();

        for (Notification notification : systemNotifications) {
            String key = notification.getTitle() + "|" + notification.getMessage();
            if (!grouped.containsKey(key)) {
                Map<String, Object> historyItem = new HashMap<>();
                historyItem.put("id", notification.getId());
                historyItem.put("title", notification.getTitle());
                historyItem.put("message", notification.getMessage());
                historyItem.put("sentAt", notification.getCreatedAt().toString());
                historyItem.put("recipientsCount", 1);
                grouped.put(key, historyItem);
            } else {
                Map<String, Object> existing = grouped.get(key);
                int currentCount = (Integer) existing.get("recipientsCount");
                existing.put("recipientsCount", currentCount + 1);
                // Prendre la date la plus récente
                if (notification.getCreatedAt().isAfter(LocalDateTime.parse(existing.get("sentAt").toString()))) {
                    existing.put("sentAt", notification.getCreatedAt().toString());
                }
            }
        }

        List<Map<String, Object>> history = grouped.values().stream()
                .sorted((a, b) -> {
                    LocalDateTime dateA = LocalDateTime.parse(a.get("sentAt").toString());
                    LocalDateTime dateB = LocalDateTime.parse(b.get("sentAt").toString());
                    return dateB.compareTo(dateA); // Plus récent en premier
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }
}

