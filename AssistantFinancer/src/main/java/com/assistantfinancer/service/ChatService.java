package com.assistantfinancer.service;

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

@Service
public class ChatService {

    @Autowired
    private WhisperService whisperService;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ResponseRepository responseRepository;

    public String processAudioQuestion(File audioFile, Long userId) throws IOException {

        // 1️⃣ audio -> texte (Whisper)
        String transcript = whisperService.transcribe(audioFile);

        // 2️⃣ récupérer le user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User non trouvé avec id = " + userId));

        // 3️⃣ sauver la Question (texte = transcript)
        Question question = new Question();
        question.setContent(transcript);
        question.setTimestamp(LocalDateTime.now());
        question.setUser(user);

        question = questionRepository.save(question);

        // 4️⃣ envoyer la question à Gemini
        String answerText = geminiService.answer(transcript);

        // 5️⃣ sauver la Response liée à la Question
        Response response = new Response();
        response.setContent(answerText);
        response.setTimestamp(LocalDateTime.now());
        response.setQuestion(question);

        responseRepository.save(response);

        // 6️⃣ renvoyer la réponse texte (pour ton Flutter / TTS)
        return answerText;
    }
}
