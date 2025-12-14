package com.assistantfinancer.repository;

import com.assistantfinancer.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCategory(String category);
    List<Course> findByLanguage(String language);
    List<Course> findByIsActive(boolean isActive);
    List<Course> findByCategoryAndLanguageAndIsActive(String category, String language, boolean isActive);
}

