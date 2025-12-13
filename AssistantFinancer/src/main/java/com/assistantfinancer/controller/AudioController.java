package com.assistantfinancer.controller;

import com.assistantfinancer.dto.ChatAnswerDto;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.UserRepository;
import com.assistantfinancer.service.ChatService;
import com.assistantfinancer.util.AudioConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class AudioController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/audio-question")
    public ResponseEntity<?> audioQuestion(
            @RequestParam("file") MultipartFile file
    ) throws IOException, InterruptedException {

        System.out.println("🎤 [AudioController] Requête audio-question reçue");
        System.out.println("📁 [AudioController] Fichier: " + (file != null ? file.getOriginalFilename() : "null") + ", taille: " + (file != null ? file.getSize() : 0));

        // Vérifier que le fichier n'est pas vide
        if (file == null || file.isEmpty() || file.getSize() == 0) {
            System.out.println("❌ [AudioController] Fichier vide ou invalide");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Le fichier audio est vide ou invalide");
        }

        // Récupérer le username depuis le SecurityContext
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🔐 [AudioController] Authentication: " + (authentication != null ? authentication.getName() : "null"));
        System.out.println("🔐 [AudioController] IsAuthenticated: " + (authentication != null ? authentication.isAuthenticated() : "null"));
        System.out.println("🔐 [AudioController] Principal: " + (authentication != null && authentication.getPrincipal() != null ? authentication.getPrincipal().toString() : "null"));
        
        if (authentication == null || authentication.getPrincipal() == null || 
            !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            System.out.println("❌ [AudioController] Non authentifié");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Non authentifié. Veuillez vous reconnecter.");
        }

        Object principal = authentication.getPrincipal();
        final String username = (principal instanceof UserDetails) 
                ? ((UserDetails) principal).getUsername() 
                : principal.toString();
        
        System.out.println("✅ [AudioController] Username extrait: " + username);

        // Récupérer l'utilisateur depuis la base de données
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + username));

        Long userId = user.getId();
        System.out.println("✅ [AudioController] User ID: " + userId);

        // 1️⃣ Enregistrer temporairement le fichier reçu
        File tempFile = File.createTempFile("audio", ".aac");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        File mp3File = null;
        try {
            // 2️⃣ Convertir AAC en MP3
            System.out.println("🔄 [AudioController] Début de la conversion AAC -> MP3");
            try {
                mp3File = AudioConverter.convertAacToMp3(tempFile);
                System.out.println("✅ [AudioController] Conversion réussie");
            } catch (IOException e) {
                System.out.println("❌ [AudioController] Erreur de conversion: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de la conversion audio. FFmpeg n'est peut-être pas installé. Détails: " + e.getMessage());
            }

            // 3️⃣ Pipeline complet : Whisper + Gemini + TTS + BD
            System.out.println("🚀 [AudioController] Début du traitement complet");
            ChatAnswerDto result = chatService.processAudioQuestion(mp3File, userId);
            System.out.println("✅ [AudioController] Traitement terminé avec succès");

            // 4️⃣ Retourner le JSON
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.out.println("❌ [AudioController] Erreur générale: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du traitement: " + e.getMessage());
        } finally {
            // Nettoyer les fichiers temporaires
            tempFile.delete();
            if (mp3File != null) mp3File.delete();
        }
    }

    @PostMapping("/text-question")
    public ResponseEntity<?> textQuestion(@RequestBody Map<String, String> request) {
        System.out.println("📝 [AudioController] Requête text-question reçue");

        String textQuestion = request.get("text");
        if (textQuestion == null || textQuestion.trim().isEmpty()) {
            System.out.println("❌ [AudioController] Question texte vide ou invalide");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La question texte est vide ou invalide");
        }
        System.out.println("💬 [AudioController] Question texte: " + textQuestion);

        // Récupérer le username depuis le SecurityContext
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || 
            !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            System.out.println("❌ [AudioController] Non authentifié");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Non authentifié. Veuillez vous reconnecter.");
        }

        Object principal = authentication.getPrincipal();
        final String username = (principal instanceof UserDetails) 
                ? ((UserDetails) principal).getUsername() 
                : principal.toString();
        
        System.out.println("✅ [AudioController] Username extrait: " + username);

        // Récupérer l'utilisateur depuis la base de données
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + username));

        Long userId = user.getId();
        System.out.println("✅ [AudioController] User ID: " + userId);

        try {
            // Traiter la question texte (sans Whisper ni TTS)
            System.out.println("🚀 [AudioController] Début du traitement texte");
            ChatAnswerDto result = chatService.processTextQuestion(textQuestion, userId);
            System.out.println("✅ [AudioController] Traitement texte terminé avec succès");

            // Retourner le JSON
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.out.println("❌ [AudioController] Erreur lors du traitement texte: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du traitement: " + e.getMessage());
        }
    }
}
