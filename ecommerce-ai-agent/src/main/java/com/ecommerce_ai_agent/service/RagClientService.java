package com.ecommerce_ai_agent.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;

@Service
public class RagClientService {
    private final WebClient webClient;
    private final String ragServerUrl;

    public RagClientService(WebClient webClient, @Value("${rag.server.url:http://localhost:8081}") String ragServerUrl) {
        this.webClient = webClient;
        this.ragServerUrl = ragServerUrl;
    }

    public String searchKnowledgeBase(String query) {
        return webClient.get()
                .uri(ragServerUrl + "/api/rag/search?q={query}", query)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
