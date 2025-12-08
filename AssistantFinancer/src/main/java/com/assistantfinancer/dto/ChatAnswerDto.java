package com.assistantfinancer.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatAnswerDto {
    private String transcript;   // texte venant de l'audio (Whisper)
    private String answerText;   // réponse générée par Gemini
    private String audioBase64;  // MP3 encodé en Base64
}
