package com.assistantfinancer.service;

import com.assistantfinancer.dto.CourseDto;
import com.assistantfinancer.model.Course;
import com.assistantfinancer.model.Quiz;
import com.assistantfinancer.model.User;
import com.assistantfinancer.model.UserProgress;
import com.assistantfinancer.repository.CourseRepository;
import com.assistantfinancer.repository.UserProgressRepository;
import com.assistantfinancer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EducationServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserProgressRepository userProgressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EducationService educationService;

    private User testUser;
    private Course testCourse;
    private Quiz testQuiz;
    private UserProgress testProgress;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testQuiz = new Quiz();
        testQuiz.setId(1L);
        testQuiz.setQuestion("Test Question?");
        testQuiz.setOptions(List.of("Option 1", "Option 2", "Option 3"));
        testQuiz.setCorrectAnswerIndex(0);
        testQuiz.setExplanation("Test explanation");

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setTitle("Test Course");
        testCourse.setDescription("Test Description");
        testCourse.setContent("Test Content");
        testCourse.setCategory("BUDGETING");
        testCourse.setDifficulty("BEGINNER");
        testCourse.setDurationMinutes(30);
        testCourse.setLanguage("FR");
        testCourse.setActive(true);
        testCourse.setQuizzes(List.of(testQuiz));

        testProgress = new UserProgress();
        testProgress.setId(1L);
        testProgress.setUser(testUser);
        testProgress.setCourse(testCourse);
        testProgress.setCompleted(false);
        testProgress.setScore(0);
        testProgress.setStartedAt(LocalDateTime.now());
    }

    @Test
    void getCourses_ShouldReturnAllActiveCourses() {
        // Given
        List<Course> courses = List.of(testCourse);
        when(courseRepository.findByIsActive(true)).thenReturn(courses);

        // When
        List<CourseDto> result = educationService.getCourses(null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Course", result.get(0).getTitle());
        verify(courseRepository, times(1)).findByIsActive(true);
    }

    @Test
    void getCourses_ShouldFilterByCategory() {
        // Given
        List<Course> courses = List.of(testCourse);
        when(courseRepository.findByCategory("BUDGETING")).thenReturn(courses);

        // When
        List<CourseDto> result = educationService.getCourses(null, "BUDGETING");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(courseRepository, times(1)).findByCategory("BUDGETING");
    }

    @Test
    void getCourses_ShouldFilterByLanguage() {
        // Given
        List<Course> courses = List.of(testCourse);
        when(courseRepository.findByLanguage("FR")).thenReturn(courses);

        // When
        List<CourseDto> result = educationService.getCourses("FR", null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(courseRepository, times(1)).findByLanguage("FR");
    }

    @Test
    void getCourses_ShouldFilterByCategoryAndLanguage() {
        // Given
        List<Course> courses = List.of(testCourse);
        when(courseRepository.findByCategoryAndLanguageAndIsActive("BUDGETING", "FR", true)).thenReturn(courses);

        // When
        List<CourseDto> result = educationService.getCourses("FR", "BUDGETING");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(courseRepository, times(1)).findByCategoryAndLanguageAndIsActive("BUDGETING", "FR", true);
    }

    @Test
    void getCourse_ShouldReturnCourseWithProgress() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userProgressRepository.findByUserAndCourse(testUser, testCourse)).thenReturn(Optional.of(testProgress));

        // When
        CourseDto result = educationService.getCourse(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals("Test Course", result.getTitle());
        assertFalse(result.isCompleted());
        assertEquals(0, result.getProgress());
        verify(courseRepository, times(1)).findById(1L);
        verify(userProgressRepository, times(1)).findByUserAndCourse(testUser, testCourse);
    }

    @Test
    void startCourse_ShouldStartNewCourse() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(userProgressRepository.findByUserAndCourse(testUser, testCourse)).thenReturn(Optional.empty());
        when(userProgressRepository.save(any(UserProgress.class))).thenAnswer(invocation -> {
            UserProgress progress = invocation.getArgument(0);
            progress.setId(1L);
            return progress;
        });

        // When
        UserProgress result = educationService.startCourse(1L, 1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getStartedAt());
        assertFalse(result.isCompleted());
        assertEquals(0, result.getScore());
        verify(userRepository, times(1)).findById(1L);
        verify(courseRepository, times(1)).findById(1L);
        verify(userProgressRepository, times(1)).save(any(UserProgress.class));
    }

    @Test
    void startCourse_ShouldResumeExistingCourse() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(userProgressRepository.findByUserAndCourse(testUser, testCourse)).thenReturn(Optional.of(testProgress));
        when(userProgressRepository.save(any(UserProgress.class))).thenReturn(testProgress);

        // When
        UserProgress result = educationService.startCourse(1L, 1L);

        // Then
        assertNotNull(result);
        verify(userProgressRepository, times(1)).save(any(UserProgress.class));
    }

    @Test
    void completeCourse_ShouldCompleteCourseWithScore() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(userProgressRepository.findByUserAndCourse(testUser, testCourse)).thenReturn(Optional.of(testProgress));
        when(userProgressRepository.save(any(UserProgress.class))).thenAnswer(invocation -> {
            UserProgress progress = invocation.getArgument(0);
            progress.setCompleted(true);
            progress.setScore(85);
            return progress;
        });

        // When
        UserProgress result = educationService.completeCourse(1L, 1L, 85);

        // Then
        assertNotNull(result);
        assertTrue(result.isCompleted());
        assertEquals(85, result.getScore());
        assertNotNull(result.getCompletedAt());
        verify(userProgressRepository, times(1)).save(any(UserProgress.class));
    }

    @Test
    void completeCourse_WithNonExistentProgress_ShouldThrowException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(userProgressRepository.findByUserAndCourse(testUser, testCourse)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            educationService.completeCourse(1L, 1L, 85);
        });

        assertEquals("Progress not found", exception.getMessage());
        verify(userProgressRepository, never()).save(any(UserProgress.class));
    }

    @Test
    void convertToDto_ShouldConvertWithQuizzes() {
        // Given
        when(courseRepository.findByIsActive(true)).thenReturn(List.of(testCourse));

        // When
        List<CourseDto> result = educationService.getCourses(null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        CourseDto dto = result.get(0);
        assertNotNull(dto.getQuizzes());
        assertEquals(1, dto.getQuizzes().size());
        assertEquals("Test Question?", dto.getQuizzes().get(0).getQuestion());
        assertEquals(3, dto.getQuizzes().get(0).getOptions().size());
        assertEquals(0, dto.getQuizzes().get(0).getCorrectAnswerIndex());
        assertEquals("Test explanation", dto.getQuizzes().get(0).getExplanation());
    }
}

