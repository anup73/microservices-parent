package com.ecommerce.springboot_mcp_server.config;

import com.ecommerce.springboot_mcp_server.tool.EcommerceTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the EcommerceTools @Tool methods with the MCP server so they are exposed
 * as callable MCP tools over Streamable HTTP/SSE.
 */
@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider ecommerceToolCallbackProvider(EcommerceTools ecommerceTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(ecommerceTools)
                .build();
    }
}
