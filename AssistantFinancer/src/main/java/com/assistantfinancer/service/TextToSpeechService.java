package com.assistantfinancer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TextToSpeechService {

    private static final String OPENAI_API_KEY = "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"; // TODO: mets ta clé
    private static final String TTS_URL = "https://api.openai.com/v1/audio/speech";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .callTimeout(180, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Génère un MP3 à partir d'un texte.
     * Retourne un tableau de bytes représentant le fichier audio.
     */
    public byte[] synthesize(String text) throws IOException {

        // Corps JSON de la requête
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4o-mini-tts");  // adapte si ton compte utilise un autre modèle TTS
        payload.put("input", text);
        payload.put("voice", "alloy");
        payload.put("format", "mp3");

        String jsonBody = objectMapper.writeValueAsString(payload);

        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(TTS_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();


        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("Erreur TTS (HTTP " + response.code() + ") : " + errorBody);
            }

            if (response.body() == null) {
                throw new IOException("Erreur TTS : body null");
            }

            // La réponse est un flux binaire (MP3)
            return response.body().bytes();

        } catch (SocketTimeoutException e) {
            // 🔴 Ici on a ton cas de timeout
            throw new IOException("Timeout lors de l'appel à l'API TTS", e);
        }
    }
}
