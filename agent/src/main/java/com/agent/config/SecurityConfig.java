package com.agent.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures this application as an OAuth2 resource server: every request must present a
 * valid JWT access token issued by the external authorization server. Token validation
 * (issuer, signature/JWK set, expiry) is auto-configured by Spring Boot from the
 * spring.security.oauth2.resourceserver.jwt.* properties in application.properties -
 * once the real authorization server exists, set issuer-uri (or jwk-set-uri) there and
 * no code changes are needed here. Only the actuator health endpoint remains public.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/prometheus").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));

        return http.build();
    }

    /**
     * Placeholder decoder used only until the real authorization server is configured
     * (spring.security.oauth2.resourceserver.jwt.issuer-uri or .jwk-set-uri). It lets the
     * app start up, but rejects every token with 401 so no request can slip through
     * unauthenticated. Once either property is set, Spring Boot auto-configures the real
     * JwtDecoder and this bean backs off automatically (condition below evaluates false).
     */
    @Bean
    @ConditionalOnExpression("'${spring.security.oauth2.resourceserver.jwt.issuer-uri:}'.isEmpty() and "
            + "'${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}'.isEmpty()")
    public JwtDecoder placeholderJwtDecoder() {
        return token -> {
            throw new BadJwtException("OAuth2 resource server is not yet configured with an authorization server "
                    + "(set spring.security.oauth2.resourceserver.jwt.issuer-uri or .jwk-set-uri).");
        };
    }
}
