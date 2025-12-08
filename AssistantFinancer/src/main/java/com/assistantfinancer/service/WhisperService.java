package com.assistantfinancer.service;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class WhisperService {

    //@Value("${openai.api.key}")
    //private String OPENAI_API_KEY;
    private static final String OPENAI_API_KEY = "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";
    private static final String WHISPER_URL = "https://api.openai.com/v1/audio/transcriptions";

    public String transcribe(File audioFile) throws IOException {

        OkHttpClient client = new OkHttpClient();

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
