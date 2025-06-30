package com.testecitel.demo.controller;

import com.testecitel.demo.service.GroqApiService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GroqController {

    @Value("${groq.api.key}")
    private String apiKey;

    @Autowired
    private GroqApiService groqApiService;

    public GroqController(GroqApiService groqApiService) {
        this.groqApiService = groqApiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody Map<String, String> req) {
        String prompt = req.get("prompt");
        return ResponseEntity.ok(groqApiService.gerarResposta(prompt));
    }

    @GetMapping("/models")
    public ResponseEntity<String> listarModelos() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.groq.com/openai/v1/models")
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return ResponseEntity.ok(response.body().string());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
