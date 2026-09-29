package com.ecommerce_ai_agent.service;

import org.springframework.ai.chat.client.ChatClient;
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
            RagClientService ragClientService, AgentTracer tracer) {
        this.ragClientService = ragClientService;
        this.tracer = tracer;
        ToolCallback ragTool = FunctionToolCallback.builder("ragSearchFunction", this::ragSearchFunction)
                .description("Search the knowledge base for store policies and general store information. Provide query.")
                .inputType(Map.class)
                .build();
        this.chatClient = builder
                .defaultSystem("You are a helpful e-commerce AI agent. " +
                        "You have access to tools via an MCP server to manage orders, products, and payments, " +
                        "and a RAG service to answer general knowledge base queries. " +
                        "Use the available MCP tools for live order, customer, payment, and product operations; " +
                        "use RAG for store policies. Always use the relevant tool for live store data. " +
                        "If a tool fails, say so clearly.")
                .defaultTools(mcpToolCallbacks, ragTool)
                .build();
    }

    public String chat(String userMessage) {
        tracer.trace("USER_QUERY", userMessage);
        try {
            String response = chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();
            tracer.trace("LLM_RESPONSE", response);
            return response;
        } catch (Exception e) {
            tracer.trace("ERROR", e.getMessage());
            throw e;
        }
    }

    public String ragSearchFunction(Map<String, Object> arguments) {
        String query = (String) arguments.get("query");
        tracer.trace("TOOL_CALL", "Calling RAG search with query: " + query);
        String result = ragClientService.searchKnowledgeBase(query);
        tracer.trace("TOOL_RESPONSE", "RAG search returned: " + result);
        return result;
    }
}
