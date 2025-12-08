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
public class ChatService {

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

        // 1️⃣ audio -> texte (Whisper)
        String transcript = whisperService.transcribe(audioFile);

        // 2️⃣ récupérer le user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User non trouvé avec id = " + userId));

        // 3️⃣ sauver la Question
        Question question = new Question();
        question.setContent(transcript);
        question.setTimestamp(LocalDateTime.now());
        question.setUser(user);
        question = questionRepository.save(question);

        // 4️⃣ envoyer la question à Gemini
        String answerText = geminiService.answer(transcript);

        // 5️⃣ TTS : texte -> MP3 (bytes)
        byte[] audioBytes = textToSpeechService.synthesize(answerText);

        // 6️⃣ encoder en Base64
        String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

        // 7️⃣ sauver la Response **avec l’audioBase64**
        Response response = new Response();
        response.setContent(answerText);
        response.setAudioBase64(audioBase64);           // 👈 ICI on stocke dans la BDD
        response.setTimestamp(LocalDateTime.now());
        response.setQuestion(question);
        responseRepository.save(response);

        // 8️⃣ construire l'objet de réponse pour le front
        ChatAnswerDto dto = new ChatAnswerDto();
        dto.setTranscript(transcript);
        dto.setAnswerText(answerText);
        dto.setAudioBase64(audioBase64);

        return dto;
    }
}
