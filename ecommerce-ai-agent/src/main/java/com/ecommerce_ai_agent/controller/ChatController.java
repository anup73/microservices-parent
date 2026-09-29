package com.ecommerce_ai_agent.controller;

import com.ecommerce_ai_agent.service.AiAgentService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AiAgentService aiAgentService;

    public ChatController(AiAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    @PostMapping("/message")
    public String sendMessage(@RequestBody String message) {
        return aiAgentService.chat(message);
    }
}
