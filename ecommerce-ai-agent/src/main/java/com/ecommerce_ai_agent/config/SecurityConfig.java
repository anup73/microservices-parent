package com.ecommerce_ai_agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/api/chat/**", "/static/**", "/actuator/health", "/actuator/health/**", "/actuator/prometheus").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    @Primary
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
        manager.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build());
        return manager;
    }

    @Bean
    public McpClientCustomizer<HttpClientStreamableHttpTransport.Builder> mcpOAuth2RequestCustomizer(
            OAuth2AuthorizedClientManager authorizedClientManager) {
        return (connectionName, transportBuilder) -> transportBuilder.httpRequestCustomizer(
                (request, method, endpoint, body, context) -> {
            var authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("mcp-server")
                    .principal("ecommerce-ai-agent")
                    .build();
            var authorizedClient = authorizedClientManager.authorize(authorizeRequest);
            if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
                throw new IllegalStateException("Could not obtain an OAuth2 access token for the MCP server");
            }
            request.header("Authorization", "Bearer " + authorizedClient.getAccessToken().getTokenValue());
        });
    }
}
