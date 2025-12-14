package com.assistantfinancer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDto {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private Boolean enabled;
    private Integer totalPoints;
    private Long budgetsCount;
    private Long expensesCount;
    private Long completedCoursesCount;
}

