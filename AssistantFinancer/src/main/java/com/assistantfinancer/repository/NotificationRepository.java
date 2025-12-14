package com.assistantfinancer.repository;

import com.assistantfinancer.model.Notification;
import com.assistantfinancer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);
    List<Notification> findByUserAndIsRead(User user, boolean isRead);
    List<Notification> findByUserAndScheduledForLessThanEqual(User user, LocalDateTime dateTime);
}

