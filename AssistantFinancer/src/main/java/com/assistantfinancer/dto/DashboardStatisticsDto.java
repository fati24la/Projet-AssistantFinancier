package com.assistantfinancer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsDto {
    private Long totalUsers;
    private Long totalBudgets;
    private Long totalExpenses;
    private Long totalCourses;
    private Long completedCourses;
}

