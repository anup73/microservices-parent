package com.ecommerce_ai_agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.function.FunctionToolCallback;
import com.ecommerce_ai_agent.util.AgentTracer;

@Service
public class AiAgentService {

    private final ChatClient chatClient;
    private final RagClientService ragClientService;
    private final AgentTracer tracer;

    public AiAgentService(ChatClient.Builder builder, ToolCallbackProvider mcpToolCallbacks,
            RagClientService ragClientService, AgentTracer tracer,
            @org.springframework.beans.factory.annotation.Value("${chat.memory.max-messages:50}") int maxMessages) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(maxMessages)
                .build();
        this.ragClientService = ragClientService;
        this.tracer = tracer;
        ToolCallback ragTool = FunctionToolCallback.builder("ragSearchFunction", this::ragSearchFunction)
                .description("Search the knowledge base for store policies and general store information. Provide query.")
                .inputType(RagRequest.class)
                .build();
        this.chatClient = builder
                .defaultSystem("You are a helpful e-commerce AI agent. " +
                        "You have access to tools via an MCP server to manage orders, products, and payments, " +
                        "and a RAG service to answer general knowledge base queries. " +
                        "Use the available MCP tools for live order, customer, payment, and inventory/stock operations; " +
                        "use ragSearchFunction for product catalog and discovery questions (browsing products, categories, " +
                        "specs, descriptions, comparisons, SKU lookups, recommendations) and for store policies " +
                        "(returns, shipping, warranty). Search RAG first for any product search by name, category or SKU; " +
                        "use MCP only when live stock or order data is needed. " +
                        "If a tool fails, say so clearly.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(mcpToolCallbacks, ragTool)
                .build();
    }

    public String chat(String conversationId, String userMessage) {
        tracer.trace("USER_QUERY", userMessage);
        try {
            String response = chatClient.prompt()
                    .user(userMessage)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();
            tracer.trace("LLM_RESPONSE", response);
            return response;
        } catch (Exception e) {
            tracer.trace("ERROR", e.getMessage());
            throw e;
        }
    }

    public record RagRequest(
            @org.springframework.ai.tool.annotation.ToolParam(description = "Search query about store policies (returns, shipping, warranty, etc.)") String query) {
    }

    public String ragSearchFunction(RagRequest request) {
        String query = request.query();
        tracer.trace("TOOL_CALL", "Calling RAG search with query: " + query);
        String result = ragClientService.searchKnowledgeBase(query);
        tracer.trace("TOOL_RESPONSE", "RAG search returned: " + result);
        return result;
    }
}
