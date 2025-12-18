package com.assistantfinancer.service;

import com.assistantfinancer.dto.ChatAnswerDto;
import com.assistantfinancer.model.Question;
import com.assistantfinancer.model.Response;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.QuestionRepository;
import com.assistantfinancer.repository.ResponseRepository;
import com.assistantfinancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class  ChatService {

    @Autowired
    private WhisperService whisperService;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private TextToSpeechService textToSpeechService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ResponseRepository responseRepository;

    public ChatAnswerDto processAudioQuestion(File audioFile, Long userId) throws IOException {
        System.out.println("🔵 [ChatService] Début du traitement pour userId: " + userId);

        // 1️⃣ audio -> texte (Whisper)
        System.out.println("🔵 [ChatService] Étape 1/5: Transcription audio avec Whisper...");
        String transcript = whisperService.transcribe(audioFile);
        System.out.println("✅ [ChatService] Transcription terminée: " + (transcript != null ? transcript.substring(0, Math.min(50, transcript.length())) + "..." : "null"));

        // 2️⃣ récupérer le user
        System.out.println("🔵 [ChatService] Étape 2/5: Récupération de l'utilisateur...");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User non trouvé avec id = " + userId));

        // 3️⃣ sauver la Question
        System.out.println("🔵 [ChatService] Étape 3/5: Sauvegarde de la question...");
        Question question = new Question();
        question.setContent(transcript);
        question.setTimestamp(LocalDateTime.now());
        question.setUser(user);
        question = questionRepository.save(question);
        System.out.println("✅ [ChatService] Question sauvegardée (ID: " + question.getId() + ")");

        // 4️⃣ envoyer la question à Gemini
        System.out.println("🔵 [ChatService] Étape 4/5: Génération de la réponse avec Gemini...");
        String answerText = geminiService.answer(transcript);
        System.out.println("✅ [ChatService] Réponse Gemini générée: " + (answerText != null ? answerText.substring(0, Math.min(50, answerText.length())) + "..." : "null"));

        // 5️⃣ TTS : texte -> MP3 (bytes)
        System.out.println("🔵 [ChatService] Étape 5/5: Synthèse vocale (TTS)...");
        byte[] audioBytes = textToSpeechService.synthesize(answerText);
        System.out.println("✅ [ChatService] Audio TTS généré (" + audioBytes.length + " bytes)");

        // 6️⃣ encoder en Base64
        System.out.println("🔵 [ChatService] Encodage Base64 de l'audio...");
        String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);
        System.out.println("✅ [ChatService] Audio encodé (" + audioBase64.length() + " caractères)");

        // 7️⃣ sauver la Response (sans audioBase64 car trop volumineux pour MySQL)
        System.out.println("🔵 [ChatService] Sauvegarde de la réponse...");
        Response response = new Response();
        response.setContent(answerText);
        response.setAudioBase64(null); // Ne pas sauvegarder l'audio dans la BDD (trop volumineux)
        response.setTimestamp(LocalDateTime.now());
        response.setQuestion(question);
        responseRepository.save(response);
        System.out.println("✅ [ChatService] Réponse sauvegardée (ID: " + response.getId() + ") - Audio envoyé uniquement au frontend");

        // 8️⃣ construire l'objet de réponse pour le front
        ChatAnswerDto dto = new ChatAnswerDto();
        dto.setTranscript(transcript);
        dto.setAnswerText(answerText);
        dto.setAudioBase64(audioBase64);

        System.out.println("✅ [ChatService] Traitement terminé avec succès!");
        return dto;
    }

    public ChatAnswerDto processTextQuestion(String questionText, Long userId) {
        System.out.println("🔵 [ChatService] Début du traitement texte pour userId: " + userId);

        // 1️⃣ récupérer le user
        System.out.println("🔵 [ChatService] Étape 1/3: Récupération de l'utilisateur...");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User non trouvé avec id = " + userId));

        // 2️⃣ sauver la Question
        System.out.println("🔵 [ChatService] Étape 2/3: Sauvegarde de la question...");
        Question question = new Question();
        question.setContent(questionText);
        question.setTimestamp(LocalDateTime.now());
        question.setUser(user);
        question = questionRepository.save(question);
        System.out.println("✅ [ChatService] Question sauvegardée (ID: " + question.getId() + ")");

        // 3️⃣ envoyer la question à Gemini
        System.out.println("🔵 [ChatService] Étape 3/3: Génération de la réponse avec Gemini...");
        String answerText = geminiService.answer(questionText);
        System.out.println("✅ [ChatService] Réponse Gemini générée: " + (answerText != null ? answerText.substring(0, Math.min(50, answerText.length())) + "..." : "null"));

        // 4️⃣ sauver la Response (sans audioBase64 pour les questions texte)
        System.out.println("🔵 [ChatService] Sauvegarde de la réponse...");
        Response response = new Response();
        response.setContent(answerText);
        response.setAudioBase64(null); // Pas d'audio pour les réponses texte
        response.setTimestamp(LocalDateTime.now());
        response.setQuestion(question);
        responseRepository.save(response);
        System.out.println("✅ [ChatService] Réponse sauvegardée (ID: " + response.getId() + ")");

        // 5️⃣ construire l'objet de réponse pour le front (sans audioBase64)
        ChatAnswerDto dto = new ChatAnswerDto();
        dto.setTranscript(questionText); // Pour les questions texte, transcript = questionText
        dto.setAnswerText(answerText);
        dto.setAudioBase64(null); // Pas d'audio pour les réponses texte

        System.out.println("✅ [ChatService] Traitement texte terminé avec succès!");
        return dto;
    }
}
