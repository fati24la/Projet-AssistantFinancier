package com.assistantfinancer.controller;

import com.assistantfinancer.dto.AdminCourseDto;
import com.assistantfinancer.dto.CourseDto;
import com.assistantfinancer.model.Course;
import com.assistantfinancer.model.Quiz;
import com.assistantfinancer.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping
    public ResponseEntity<List<CourseDto>> getCourses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean isActive) {

        List<Course> courses;

        if (category != null && language != null && isActive != null) {
            courses = courseRepository.findByCategoryAndLanguageAndIsActive(category, language, isActive);
        } else if (category != null && language != null) {
            courses = courseRepository.findAll().stream()
                    .filter(c -> c.getCategory().equals(category) && c.getLanguage().equals(language))
                    .collect(Collectors.toList());
        } else if (category != null) {
            courses = courseRepository.findByCategory(category);
        } else if (language != null) {
            courses = courseRepository.findByLanguage(language);
        } else if (isActive != null) {
            courses = courseRepository.findByIsActive(isActive);
        } else {
            courses = courseRepository.findAll();
        }

        List<CourseDto> courseDtos = courses.stream().map(course -> {
            CourseDto dto = new CourseDto();
            dto.setId(course.getId());
            dto.setTitle(course.getTitle());
            dto.setDescription(course.getDescription());
            dto.setContent(course.getContent());
            dto.setCategory(course.getCategory());
            dto.setDifficulty(course.getDifficulty());
            dto.setDurationMinutes(course.getDurationMinutes());
            dto.setLanguage(course.getLanguage());
            dto.setCompleted(false);
            dto.setProgress(0);

            if (course.getQuizzes() != null) {
                List<CourseDto.QuizDto> quizDtos = course.getQuizzes().stream()
                        .map(quiz -> {
                            CourseDto.QuizDto qdto = new CourseDto.QuizDto();
                            qdto.setId(quiz.getId());
                            qdto.setQuestion(quiz.getQuestion());
                            qdto.setOptions(quiz.getOptions());
                            qdto.setCorrectAnswerIndex(quiz.getCorrectAnswerIndex());
                            qdto.setExplanation(quiz.getExplanation());
                            return qdto;
                        })
                        .collect(Collectors.toList());
                dto.setQuizzes(quizDtos);
            }

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(courseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setContent(course.getContent());
        dto.setCategory(course.getCategory());
        dto.setDifficulty(course.getDifficulty());
        dto.setDurationMinutes(course.getDurationMinutes());
        dto.setLanguage(course.getLanguage());
        dto.setCompleted(false);
        dto.setProgress(0);

        if (course.getQuizzes() != null) {
            List<CourseDto.QuizDto> quizDtos = course.getQuizzes().stream()
                    .map(quiz -> {
                        CourseDto.QuizDto qdto = new CourseDto.QuizDto();
                        qdto.setId(quiz.getId());
                        qdto.setQuestion(quiz.getQuestion());
                        qdto.setOptions(quiz.getOptions());
                        qdto.setCorrectAnswerIndex(quiz.getCorrectAnswerIndex());
                        qdto.setExplanation(quiz.getExplanation());
                        return qdto;
                    })
                    .collect(Collectors.toList());
            dto.setQuizzes(quizDtos);
        }

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<CourseDto> createCourse(@RequestBody AdminCourseDto courseDto) {
        Course course = new Course();
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setContent(courseDto.getContent());
        course.setCategory(courseDto.getCategory());
        course.setDifficulty(courseDto.getDifficulty());
        course.setDurationMinutes(courseDto.getDurationMinutes());
        course.setLanguage(courseDto.getLanguage());
        course.setActive(courseDto.getIsActive() != null ? courseDto.getIsActive() : true);
        course.setCreatedAt(LocalDateTime.now());

        Course saved = courseRepository.save(course);

        CourseDto dto = new CourseDto();
        dto.setId(saved.getId());
        dto.setTitle(saved.getTitle());
        dto.setDescription(saved.getDescription());
        dto.setContent(saved.getContent());
        dto.setCategory(saved.getCategory());
        dto.setDifficulty(saved.getDifficulty());
        dto.setDurationMinutes(saved.getDurationMinutes());
        dto.setLanguage(saved.getLanguage());

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long id, @RequestBody AdminCourseDto courseDto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setContent(courseDto.getContent());
        course.setCategory(courseDto.getCategory());
        course.setDifficulty(courseDto.getDifficulty());
        course.setDurationMinutes(courseDto.getDurationMinutes());
        course.setLanguage(courseDto.getLanguage());
        if (courseDto.getIsActive() != null) {
            course.setActive(courseDto.getIsActive());
        }

        Course saved = courseRepository.save(course);

        CourseDto dto = new CourseDto();
        dto.setId(saved.getId());
        dto.setTitle(saved.getTitle());
        dto.setDescription(saved.getDescription());
        dto.setContent(saved.getContent());
        dto.setCategory(saved.getCategory());
        dto.setDifficulty(saved.getDifficulty());
        dto.setDurationMinutes(saved.getDurationMinutes());
        dto.setLanguage(saved.getLanguage());

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        courseRepository.delete(course);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<CourseDto> toggleCourseStatus(@PathVariable Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setActive(!course.isActive());
        Course saved = courseRepository.save(course);

        CourseDto dto = new CourseDto();
        dto.setId(saved.getId());
        dto.setTitle(saved.getTitle());
        dto.setDescription(saved.getDescription());
        dto.setContent(saved.getContent());
        dto.setCategory(saved.getCategory());
        dto.setDifficulty(saved.getDifficulty());
        dto.setDurationMinutes(saved.getDurationMinutes());
        dto.setLanguage(saved.getLanguage());

        return ResponseEntity.ok(dto);
    }
}

