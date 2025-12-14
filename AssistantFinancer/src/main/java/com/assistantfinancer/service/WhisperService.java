package com.assistantfinancer.service;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class WhisperService {

    //@Value("${openai.api.key}")
    //private String OPENAI_API_KEY;
    private static final String OPENAI_API_KEY = "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";
    private static final String WHISPER_URL = "https://api.openai.com/v1/audio/transcriptions";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .callTimeout(180, TimeUnit.SECONDS)
            .build();

    public String transcribe(File audioFile) throws IOException {

        MediaType mediaType = MediaType.parse("audio/mpeg"); // ou audio/wav selon ton fichier
        RequestBody fileBody = RequestBody.create(mediaType, audioFile);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.getName(), fileBody)
                .addFormDataPart("model", "gpt-4o-mini-transcribe")
                .build();

        Request request = new Request.Builder()
                .url(WHISPER_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Erreur transcription : " + response.body().string());
        }

        String responseBody = response.body().string();
        // Extraire le texte du JSON
        String text = responseBody.split("\"text\":\"")[1].split("\"")[0];
        return text;
    }
}
