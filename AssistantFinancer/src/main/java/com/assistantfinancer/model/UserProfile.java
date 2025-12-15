package com.assistantfinancer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String language; // FR, AR, AMZ
    private String level; // BEGINNER, INTERMEDIATE, ADVANCED
    private Integer points;
    private Integer levelNumber; // 1-10
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal totalSavings;
    private BigDecimal totalDebt;
    private String financialGoals; // JSON string
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
        name = "user_badges",
        joinColumns = @JoinColumn(name = "user_profile_id"),
        inverseJoinColumns = @JoinColumn(name = "badge_id")
    )
    @ToString.Exclude
    private Set<Badge> badges;
}

