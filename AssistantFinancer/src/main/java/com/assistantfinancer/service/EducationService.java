package com.assistantfinancer.service;

import com.assistantfinancer.dto.CourseDto;
import com.assistantfinancer.model.Course;
import com.assistantfinancer.model.User;
import com.assistantfinancer.model.UserProgress;
import com.assistantfinancer.repository.CourseRepository;
import com.assistantfinancer.repository.UserProgressRepository;
import com.assistantfinancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EducationService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private UserRepository userRepository;

    public List<CourseDto> getCourses(String language, String category) {
        List<Course> courses;
        
        if (category != null && language != null) {
            courses = courseRepository.findByCategoryAndLanguageAndIsActive(category, language, true);
        } else if (category != null) {
            courses = courseRepository.findByCategory(category).stream()
                    .filter(Course::isActive)
                    .collect(Collectors.toList());
        } else if (language != null) {
            courses = courseRepository.findByLanguage(language).stream()
                    .filter(Course::isActive)
                    .collect(Collectors.toList());
        } else {
            courses = courseRepository.findByIsActive(true);
        }

        return courses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CourseDto getCourse(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseDto dto = convertToDto(course);
        
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElse(null);
            if (user != null) {
                userProgressRepository.findByUserAndCourse(user, course)
                        .ifPresent(progress -> {
                            dto.setCompleted(progress.isCompleted());
                            dto.setProgress(progress.getScore());
                        });
            }
        }

        return dto;
    }

    public UserProgress startCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        UserProgress progress = userProgressRepository.findByUserAndCourse(user, course)
                .orElse(new UserProgress());

        if (progress.getId() == null) {
            progress.setUser(user);
            progress.setCourse(course);
            progress.setStartedAt(LocalDateTime.now());
            progress.setCompleted(false);
            progress.setScore(0);
        }

        return userProgressRepository.save(progress);
    }

    public UserProgress completeCourse(Long userId, Long courseId, Integer score) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        UserProgress progress = userProgressRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new RuntimeException("Progress not found"));

        progress.setCompleted(true);
        progress.setScore(score);
        progress.setCompletedAt(LocalDateTime.now());

        return userProgressRepository.save(progress);
    }

    private CourseDto convertToDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setContent(course.getContent());
        dto.setCategory(course.getCategory());
        dto.setDifficulty(course.getDifficulty());
        dto.setDurationMinutes(course.getDurationMinutes());
        dto.setLanguage(course.getLanguage());
        dto.setIsActive(course.isActive());
        dto.setCompleted(false);
        dto.setProgress(0);
        
        if (course.getQuizzes() != null) {
            dto.setQuizzes(course.getQuizzes().stream()
                    .map(quiz -> {
                        CourseDto.QuizDto quizDto = new CourseDto.QuizDto();
                        quizDto.setId(quiz.getId());
                        quizDto.setQuestion(quiz.getQuestion());
                        quizDto.setOptions(quiz.getOptions());
                        quizDto.setCorrectAnswerIndex(quiz.getCorrectAnswerIndex());
                        quizDto.setExplanation(quiz.getExplanation());
                        return quizDto;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}

