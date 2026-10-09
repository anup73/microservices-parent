package com.ecommerce_auth_server.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Logs every inbound request to the /oauth2/token endpoint before it's handled by
 * Spring Authorization Server's OAuth2TokenEndpointFilter, capturing the requesting
 * client id and grant type. Never logs the client secret.
 */
public class OAuth2TokenRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OAuth2TokenRequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("/oauth2/token".equals(request.getRequestURI())) {
            log.info("OAuth2 token request received: clientId='{}' grantType='{}' remoteAddr='{}'",
                    resolveClientId(request), request.getParameter("grant_type"), request.getRemoteAddr());
        }
        filterChain.doFilter(request, response);
    }

    private String resolveClientId(HttpServletRequest request) {
        String clientId = request.getParameter("client_id");
        if (clientId != null && !clientId.isBlank()) {
            return clientId;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.toLowerCase().startsWith("basic ")) {
            try {
                String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)), StandardCharsets.UTF_8);
                int colonIndex = decoded.indexOf(':');
                return colonIndex > 0 ? decoded.substring(0, colonIndex) : "unknown";
            } catch (IllegalArgumentException e) {
                return "unknown";
            }
        }
        return "unknown";
    }
}
