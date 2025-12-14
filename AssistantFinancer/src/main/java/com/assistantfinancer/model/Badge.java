package com.assistantfinancer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String icon; // emoji or icon name
    private String category; // BUDGET, SAVINGS, EDUCATION, CHALLENGE
    private String requirement; // JSON string describing requirements

    @ManyToMany(mappedBy = "badges")
    private Set<UserProfile> userProfiles;
}

