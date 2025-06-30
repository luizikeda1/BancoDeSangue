package com.testecitel.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiApiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent";

    public String gerarResposta(String prompt) {
        try {
            OkHttpClient client = new OkHttpClient();

            String json = """
                    {
                      "contents": [
                        {
                          "role": "user",
                          "parts": [
                            {
                              "text": "%s"
                            }
                          ]
                        }
                      ]
                    }
                    """.formatted(prompt);

            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

            HttpUrl url = HttpUrl.parse(ENDPOINT)
                    .newBuilder()
                    .addQueryParameter("key", apiKey)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Sem corpo de resposta";
                    return "Erro da API: " + response.code() + " - " + errorBody;
                }

                String bodyStr = response.body().string();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(bodyStr);
                return jsonNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            }

        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }
}