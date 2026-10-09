package com.agent.service;

import com.agent.config.AmazonSpApiProperties;
import com.agent.dto.AmazonAccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Profile("amazon")
public class AmazonLwaService {

    private static final Logger log = LoggerFactory.getLogger(AmazonLwaService.class);

    private final WebClient amazonLwaWebClient;
    private final AmazonSpApiProperties properties;

    public AmazonLwaService(WebClient amazonLwaWebClient, AmazonSpApiProperties properties) {
        this.amazonLwaWebClient = amazonLwaWebClient;
        this.properties = properties;
    }

    public Mono<AmazonAccessTokenResponse> getAccessToken() {
        log.info("Requesting Amazon LWA access token");
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", properties.getLwaRefreshToken());
        formData.add("client_id", properties.getLwaClientId());
        formData.add("client_secret", properties.getLwaClientSecret());

        return amazonLwaWebClient.post()
                .uri("/auth/o2/token")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(AmazonAccessTokenResponse.class)
                .doOnSuccess(token -> log.info("Amazon LWA access token obtained"))
                .doOnError(error -> log.error("Amazon LWA token request failed: {}", error.getMessage()));
    }
}
