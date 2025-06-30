package com.testecitel.demo.controller;

import com.testecitel.demo.service.GeminiApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiApiService geminiApiService;

    public GeminiController(GeminiApiService geminiApiService) {
        this.geminiApiService = geminiApiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody Map<String, String> req) {
        String prompt = req.get("prompt");
        return ResponseEntity.ok(geminiApiService.gerarResposta(prompt));
    }
}
