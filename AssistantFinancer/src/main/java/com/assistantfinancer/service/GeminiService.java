package com.assistantfinancer.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final Client client;

    // On construit le client Gemini une seule fois avec la clé API
    public GeminiService(@Value("${gemini.api.key}") String apiKey) {
        this.client = Client
                .builder()
                .apiKey(apiKey)   // clé lue depuis application.properties
                .build();
    }

    // questionText = texte généré par Whisper (OpenAI)
    public String answer(String questionText) {

        // Appel simple : texte -> modèle Gemini
        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.5-flash",   // modèle Gemini
                        questionText,         // ta question (transcript)
                        null                  // pas de config avancée pour l’instant
                );

        // Récupérer le texte de la réponse
        return response.text();
    }
}
