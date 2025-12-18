package com.assistantfinancer.controller;

import com.assistantfinancer.dto.AdminCourseDto;
import com.assistantfinancer.dto.CourseDto;
import com.assistantfinancer.model.Course;
import com.assistantfinancer.model.Quiz;
import com.assistantfinancer.repository.CourseRepository;
import com.assistantfinancer.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private com.assistantfinancer.repository.UserProgressRepository userProgressRepository;

    @Autowired
    private QuizRepository quizRepository;

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
            dto.setIsActive(course.isActive());
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
        dto.setIsActive(course.isActive());
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
        dto.setIsActive(saved.isActive());

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
        dto.setIsActive(saved.isActive());

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        try {
            System.out.println("🗑️ [AdminCourseController] Tentative de suppression du cours ID: " + id);
            
            Course course = courseRepository.findById(id).orElse(null);
            if (course == null) {
                System.out.println("❌ [AdminCourseController] Cours non trouvé: " + id);
                return ResponseEntity.status(404).body(java.util.Map.of("message", "Course not found with id: " + id));
            }
            
            System.out.println("✅ [AdminCourseController] Cours trouvé: " + course.getTitle());
            
            // Supprimer d'abord les données liées pour éviter les erreurs de contrainte
            // Supprimer les progressions utilisateur liées à ce cours en utilisant une requête JPQL
            // pour éviter les problèmes de session Hibernate (suppression directe sans charger les entités)
            try {
                int deletedCount = userProgressRepository.deleteByCourseId(id);
                System.out.println("📚 Suppression de " + deletedCount + " progressions utilisateur");
            } catch (Exception e) {
                System.out.println("⚠️ Erreur lors de la suppression des progressions: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Les quiz sont supprimés automatiquement grâce à CascadeType.ALL
            // Pas besoin de les supprimer manuellement
            
            // Maintenant supprimer le cours (les quiz seront supprimés en cascade)
            System.out.println("🗑️ Suppression finale du cours: " + course.getTitle());
            courseRepository.delete(course);
            // La transaction @Transactional gérera le commit automatiquement à la fin de la méthode
            
            System.out.println("✅ [AdminCourseController] Cours supprimé avec succès");
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            System.err.println("❌ [AdminCourseController] RuntimeException: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
            return ResponseEntity.status(404).body(java.util.Map.of("message", "Course not found", "error", errorMsg));
        } catch (Exception e) {
            System.err.println("❌ [AdminCourseController] Exception: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
            return ResponseEntity.status(500).body(java.util.Map.of("message", "Error deleting course", "error", errorMsg));
        }
    }

    @PutMapping("/{id}/toggle-status")
    @Transactional
    public ResponseEntity<?> toggleCourseStatus(@PathVariable Long id) {
        try {
            System.out.println("🔄 [AdminCourseController] Toggle status pour le cours ID: " + id);
            
            Course course = courseRepository.findById(id).orElse(null);
            if (course == null) {
                System.out.println("❌ [AdminCourseController] Cours non trouvé: " + id);
                return ResponseEntity.status(404).body(java.util.Map.of("message", "Course not found with id: " + id));
            }
            
            // Toggle le statut isActive
            boolean currentStatus = course.isActive();
            boolean newStatus = !currentStatus;
            course.setActive(newStatus);
            Course saved = courseRepository.save(course);
            
            System.out.println("✅ [AdminCourseController] Statut du cours " + saved.getTitle() + " changé de " + currentStatus + " à " + newStatus);
            
            CourseDto dto = new CourseDto();
            dto.setId(saved.getId());
            dto.setTitle(saved.getTitle());
            dto.setDescription(saved.getDescription());
            dto.setContent(saved.getContent());
            dto.setCategory(saved.getCategory());
            dto.setDifficulty(saved.getDifficulty());
            dto.setDurationMinutes(saved.getDurationMinutes());
            dto.setLanguage(saved.getLanguage());
            dto.setIsActive(saved.isActive());

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            System.err.println("❌ [AdminCourseController] Erreur lors du toggle status: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
            return ResponseEntity.status(500).body(java.util.Map.of("message", "Error toggling course status", "error", errorMsg));
        }
    }

    // ---------------- QUIZZES MANAGEMENT (ADMIN) ----------------

    @PostMapping("/{courseId}/quizzes")
    @Transactional
    public ResponseEntity<?> addQuizToCourse(
            @PathVariable Long courseId,
            @RequestBody CourseDto.QuizDto quizDto) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

            Quiz quiz = new Quiz();
            quiz.setQuestion(quizDto.getQuestion());
            quiz.setOptions(quizDto.getOptions());
            quiz.setCorrectAnswerIndex(quizDto.getCorrectAnswerIndex());
            quiz.setExplanation(quizDto.getExplanation());
            quiz.setCourse(course);

            Quiz saved = quizRepository.save(quiz);

            CourseDto.QuizDto response = new CourseDto.QuizDto();
            response.setId(saved.getId());
            response.setQuestion(saved.getQuestion());
            response.setOptions(saved.getOptions());
            response.setCorrectAnswerIndex(saved.getCorrectAnswerIndex());
            response.setExplanation(saved.getExplanation());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("❌ [AdminCourseController] Error adding quiz: " + e.getMessage());
            return ResponseEntity.status(404).body(java.util.Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [AdminCourseController] Unexpected error adding quiz: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("message", "Error creating quiz", "error", e.getMessage()));
        }
    }

    @PutMapping("/{courseId}/quizzes/{quizId}")
    @Transactional
    public ResponseEntity<?> updateQuiz(
            @PathVariable Long courseId,
            @PathVariable Long quizId,
            @RequestBody CourseDto.QuizDto quizDto) {
        try {
            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

            if (quiz.getCourse() == null || !quiz.getCourse().getId().equals(courseId)) {
                return ResponseEntity.status(404).body(java.util.Map.of("message", "Quiz does not belong to this course"));
            }

            quiz.setQuestion(quizDto.getQuestion());
            quiz.setOptions(quizDto.getOptions());
            quiz.setCorrectAnswerIndex(quizDto.getCorrectAnswerIndex());
            quiz.setExplanation(quizDto.getExplanation());

            Quiz saved = quizRepository.save(quiz);

            CourseDto.QuizDto response = new CourseDto.QuizDto();
            response.setId(saved.getId());
            response.setQuestion(saved.getQuestion());
            response.setOptions(saved.getOptions());
            response.setCorrectAnswerIndex(saved.getCorrectAnswerIndex());
            response.setExplanation(saved.getExplanation());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("❌ [AdminCourseController] Error updating quiz: " + e.getMessage());
            return ResponseEntity.status(404).body(java.util.Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [AdminCourseController] Unexpected error updating quiz: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("message", "Error updating quiz", "error", e.getMessage()));
        }
    }

    @DeleteMapping("/{courseId}/quizzes/{quizId}")
    @Transactional
    public ResponseEntity<?> deleteQuiz(
            @PathVariable Long courseId,
            @PathVariable Long quizId) {
        try {
            Quiz quiz = quizRepository.findById(quizId).orElse(null);
            if (quiz == null || quiz.getCourse() == null || !quiz.getCourse().getId().equals(courseId)) {
                return ResponseEntity.status(404).body(java.util.Map.of("message", "Quiz not found with id: " + quizId));
            }

            quizRepository.delete(quiz);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("❌ [AdminCourseController] Error deleting quiz: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("message", "Error deleting quiz", "error", e.getMessage()));
        }
    }
}

