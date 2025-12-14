package com.assistantfinancer.repository;

import com.assistantfinancer.model.SavingsGoal;
import com.assistantfinancer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByUser(User user);
    List<SavingsGoal> findByUserAndCompleted(User user, boolean completed);
}

