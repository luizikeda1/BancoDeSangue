package com.testecitel.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GroqApiService {

    @Value("${groq.api.key}")
    private String apiKey;

    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

    public String gerarResposta(String prompt) {
        try {
            OkHttpClient client = new OkHttpClient();

            String json = """
                    {
                      "model": "llama3-70b-8192",
                      "messages": [
                        { "role": "user", "content": "%s" }
                      ]
                    }
                    """.formatted(prompt);


            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(ENDPOINT)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body().string();
                    return "Erro da API: " + response.code() + " - " + errorBody;
                }

                String bodyStr = response.body().string();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(bodyStr);
                return jsonNode.path("choices").get(0).path("message").path("content").asText();
            }

        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }
}
