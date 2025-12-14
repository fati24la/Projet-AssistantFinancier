package com.assistantfinancer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCourseDto {
    private String title;
    private String description;
    private String content;
    private String category;
    private String difficulty;
    private Integer durationMinutes;
    private String language;
    private Boolean isActive;
}

