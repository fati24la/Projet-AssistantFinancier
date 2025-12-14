package com.assistantfinancer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressDto {
    private Long id;
    private Boolean completed;
    private Integer score;
    private String startedAt;
    private String completedAt;
    private CourseDto course;
}

