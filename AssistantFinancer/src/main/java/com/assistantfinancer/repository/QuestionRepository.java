package com.assistantfinancer.repository;

import com.assistantfinancer.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {}
