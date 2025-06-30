package com.testecitel.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.stereotype.Service;

@Service
public class GeminiApiService {

    private static final String API_KEY = "AIzaSyBewwPjgDvqnMRIRIhYH4nTtB8-b6ZMYqk";
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

    public String gerarResposta(String prompt) {
        try {
            OkHttpClient client = new OkHttpClient();

            String json = """
                {
                  "contents": [{
                    "parts": [{
                      "text": "%s"
                    }]
                  }]
                }
            """.formatted(prompt);

            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            Request request = new Request.Builder().url(ENDPOINT).post(body).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "Erro da API: " + response.code();
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
