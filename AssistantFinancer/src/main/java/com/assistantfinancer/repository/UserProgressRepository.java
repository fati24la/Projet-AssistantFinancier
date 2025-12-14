package com.assistantfinancer.repository;

import com.assistantfinancer.model.Course;
import com.assistantfinancer.model.User;
import com.assistantfinancer.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUser(User user);
    Optional<UserProgress> findByUserAndCourse(User user, Course course);
    List<UserProgress> findByUserAndCompleted(User user, boolean completed);
}

