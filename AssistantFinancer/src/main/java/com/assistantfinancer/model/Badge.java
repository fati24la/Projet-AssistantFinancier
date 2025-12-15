package com.assistantfinancer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String name;
    private String description;
    private String icon; // emoji or icon name
    private String category; // BUDGET, SAVINGS, EDUCATION, CHALLENGE
    private String requirement; // JSON string describing requirements

    @ManyToMany(mappedBy = "badges")
    @ToString.Exclude
    private Set<UserProfile> userProfiles;
}

