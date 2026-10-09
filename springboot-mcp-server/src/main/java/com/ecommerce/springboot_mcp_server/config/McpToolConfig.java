package com.ecommerce.springboot_mcp_server.config;

import com.ecommerce.springboot_mcp_server.tool.EcommerceTools;
import com.ecommerce.springboot_mcp_server.tool.FileSystemTools;
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
    public ToolCallbackProvider ecommerceToolCallbackProvider(    EcommerceTools ecommerceTools,
                                                                   FileSystemTools fileSystemTools) {
            return MethodToolCallbackProvider.builder()
                    .toolObjects(ecommerceTools, fileSystemTools)
                .build();
    }
}
