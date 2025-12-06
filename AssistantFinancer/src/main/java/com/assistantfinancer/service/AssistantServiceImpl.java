package com.assistantfinancer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.assistantfinancer.model.Question;
import com.assistantfinancer.model.Response;
import com.assistantfinancer.model.User;
import com.assistantfinancer.repository.QuestionRepository;
import com.assistantfinancer.repository.ResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class AssistantServiceImpl implements AssistantService {

    private final QuestionRepository questionRepository;
    private final ResponseRepository responseRepository;
    private final WebClient webClient;

    private final String GEMINI_API_KEY = "AIzaSyDnOkAoJLxYJuyIs_W0Ah4Q7hDYOZyTiyw";

    public AssistantServiceImpl(QuestionRepository questionRepository,
                                ResponseRepository responseRepository) {
        this.questionRepository = questionRepository;
        this.responseRepository = responseRepository;
        this.webClient = WebClient.create("https://gemini.api.deepmind.com/v1/chat");
    }

    @Override
    public Response askQuestion(User user, String questionText) {
        // 1. Sauvegarder la question
        Question question = new Question();
        question.setContent(questionText);
        question.setUser(user);
        questionRepository.save(question);

        // 2. Appel à l'API Gemini
        String aiResponse = null;
        try {
            aiResponse = callGemini(questionText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 3. Sauvegarder la réponse
        Response response = new Response();
        response.setContent(aiResponse);
        response.setQuestion(question);
        responseRepository.save(response);

        return response;
    }

    private String callGemini(String question) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("prompt", question);
        request.put("max_tokens", 200);
        request.put("temperature", 0.7);

        // Appel WebClient
        String resultString = webClient.post()
                .header("Authorization", "Bearer " + GEMINI_API_KEY)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Parser avec Jackson
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonMap = mapper.readValue(resultString, Map.class);

        return (String) jsonMap.get("text"); // adapter selon la structure renvoyée par Gemini
    }
}
