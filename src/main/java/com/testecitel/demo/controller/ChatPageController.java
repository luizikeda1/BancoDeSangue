package com.testecitel.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatPageController {
    @GetMapping("/gemini")
    public String chatPage() {
        return "gemini/chat";
    }
}

