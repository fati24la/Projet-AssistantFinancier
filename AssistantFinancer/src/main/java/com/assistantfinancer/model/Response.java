package com.assistantfinancer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "LONGTEXT")
    private String content; // réponse générée par ChatGPT

    @Column(name = "audio_base64", columnDefinition = "LONGTEXT") // champ de audio en base64
    private String audioBase64;

    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne
    private Question question;
}

