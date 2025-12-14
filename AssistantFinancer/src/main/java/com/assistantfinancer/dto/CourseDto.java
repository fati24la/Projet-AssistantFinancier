package com.assistantfinancer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String category;
    private String difficulty;
    private Integer durationMinutes;
    private String language;
    private boolean isCompleted;
    private Integer progress;
    private List<QuizDto> quizzes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizDto {
        private Long id;
        private String question;
        private List<String> options;
        private Integer correctAnswerIndex;
        private String explanation;
    }
}

