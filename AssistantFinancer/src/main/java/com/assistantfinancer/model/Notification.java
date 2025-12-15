package com.assistantfinancer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String message;
    private String type; // BUDGET_ALERT, GOAL_REACHED, EDUCATIONAL_TIP, REMINDER
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledFor;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore // éviter la sérialisation récursive user -> notifications
    private User user;
}

