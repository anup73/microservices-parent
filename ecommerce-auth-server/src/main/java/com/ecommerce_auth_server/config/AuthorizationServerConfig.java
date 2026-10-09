package com.ecommerce_auth_server.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal OAuth2 Authorization Server configured for the client_credentials grant only
 * (machine-to-machine access, e.g. Postman or other backend services obtaining a token
 * to call the "agent" resource server). No user login/consent screens are needed for
 * this flow.
 */
@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationServerConfig.class);

    /**
     * Security filter chain for the authorization server protocol endpoints
     * (/oauth2/token, /oauth2/jwks, etc.). Must be ordered before the default chain.
     * Spring Security 7 exposes this directly as an HttpSecurity DSL method
     * (http.oauth2AuthorizationServer(...)) rather than a separate configurer class.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // Explicit patterns for the protocol endpoints exposed with a client_credentials-only
        // setup (no authorization/consent/OIDC endpoints are enabled).
        http
                .securityMatcher("/oauth2/token", "/oauth2/jwks", "/oauth2/introspect", "/oauth2/revoke",
                        "/.well-known/oauth-authorization-server")
                .oauth2AuthorizationServer(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .addFilterBefore(new OAuth2TokenRequestLoggingFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Fallback chain for any other request (e.g. actuator health). Nothing else is
     * exposed by this server, so everything not matched above is permitted.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * The single client allowed to obtain tokens for the "agent" resource server,
     * using client_credentials. Values are externalized so the secret isn't hardcoded.
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(
            @Value("${oauth.client.id}") String clientId,
            @Value("${oauth.client.secret}") String clientSecret,
            @Value("${oauth.mcp-client.id}") String mcpClientId,
            @Value("${oauth.mcp-client.secret}") String mcpClientSecret,
            @Value("${oauth.ai-agent.id}") String aiAgentClientId,
            @Value("${oauth.ai-agent.secret}") String aiAgentClientSecret) {

        log.info("Initializing RegisteredClientRepository with clients: {}, {}, {}", aiAgentClientId, mcpClientId, clientId);

        RegisteredClient agentClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("agent.read")
                .scope("agent.write")
                .clientSettings(ClientSettings.builder().build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .build())
                .build();

        // Third machine client: used by the AI Agent to obtain tokens for the MCP server.
        RegisteredClient mcpServerClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(mcpClientId)
                .clientSecret(mcpClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("mcp.read")
                .scope("mcp.write")
                .scope("agent.read")
                .scope("agent.write")
                .clientSettings(ClientSettings.builder().build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .build())
                .build();

        RegisteredClient aiAgentClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(aiAgentClientId)
                .clientSecret(aiAgentClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("agent.read")
                .scope("agent.write")
                .scope("mcp.read")
                .scope("mcp.write")
                .clientSettings(ClientSettings.builder().build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(agentClient, mcpServerClient, aiAgentClient);
    }

    /**
     * Logs once an access token has been successfully built for a client_credentials
     * request, right before it's signed and returned to the caller.
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                RegisteredClient registeredClient = context.getRegisteredClient();
                log.info("OAuth2 access token issued: clientId='{}' grantType='{}' scopes={} ttl={}",
                        registeredClient.getClientId(),
                        context.getAuthorizationGrantType().getValue(),
                        context.getAuthorizedScopes(),
                        registeredClient.getTokenSettings().getAccessTokenTimeToLive());
            }
        };
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${oauth.issuer-uri:}") String issuerUri) {
        AuthorizationServerSettings.Builder builder = AuthorizationServerSettings.builder();
        if (issuerUri != null && !issuerUri.isBlank()) {
            builder.issuer(issuerUri);
        }
        return builder.build();
    }

    /**
     * Generates an in-memory RSA key pair on startup and exposes it as a JWK set for
     * signing access tokens and for the /oauth2/jwks endpoint. Fine for local/dev use;
     * for production, persist and rotate a real key pair instead.
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }
}
