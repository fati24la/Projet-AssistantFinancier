package com.assistantfinancer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    private String category; // BUDGETING, SAVINGS, CREDIT, INSURANCE, INCLUSION
    private String difficulty; // BEGINNER, INTERMEDIATE, ADVANCED
    private Integer durationMinutes;
    private String language; // FR, AR, AMZ
    private LocalDateTime createdAt;
    private boolean isActive;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Quiz> quizzes;
}

