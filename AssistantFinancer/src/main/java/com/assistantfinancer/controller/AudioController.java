package com.assistantfinancer.controller;

import com.assistantfinancer.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/chat")
public class AudioController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/audio-question")
    public ResponseEntity<String> audioQuestion(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId
    ) throws IOException {

        // 1️⃣ MultipartFile -> fichier temporaire
        File tempFile = File.createTempFile("audio", ".mp3");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        try {
            // 2️⃣ Pipeline complet : Whisper + Gemini + BD
            String answerText = chatService.processAudioQuestion(tempFile, userId);

            // 3️⃣ Retourner la réponse texte
            return ResponseEntity.ok(answerText);
        } finally {
            tempFile.delete();
        }
    }



}
