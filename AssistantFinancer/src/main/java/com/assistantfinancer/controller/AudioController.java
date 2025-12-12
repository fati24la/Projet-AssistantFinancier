package com.assistantfinancer.controller;

import com.assistantfinancer.dto.ChatAnswerDto;
import com.assistantfinancer.service.ChatService;
import com.assistantfinancer.util.AudioConverter;
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
    public ResponseEntity<ChatAnswerDto> audioQuestion(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId
    ) throws IOException, InterruptedException {

        // 1️⃣ Enregistrer temporairement le fichier reçu
        File tempFile = File.createTempFile("audio", ".aac");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        File mp3File = null;
        try {
            // 2️⃣ Convertir AAC en MP3
            mp3File = AudioConverter.convertAacToMp3(tempFile);

            // 3️⃣ Pipeline complet : Whisper + Gemini + TTS + BD
            ChatAnswerDto result = chatService.processAudioQuestion(mp3File, userId);

            // 4️⃣ Retourner le JSON
            return ResponseEntity.ok(result);

        } finally {
            // Nettoyer les fichiers temporaires
            tempFile.delete();
            if (mp3File != null) mp3File.delete();
        }
    }
}
