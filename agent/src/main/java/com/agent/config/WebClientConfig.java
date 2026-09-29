package com.agent.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("amazon")
@EnableConfigurationProperties(AmazonSpApiProperties.class)
public class WebClientConfig {

    @Bean
    WebClient amazonLwaWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.amazon.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @Bean
    WebClient amazonSpApiWebClient(WebClient.Builder builder, AmazonSpApiProperties properties) {
        WebClient.Builder clientBuilder = builder
                .baseUrl(properties.getEndpoint())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        if (StringUtils.hasText(properties.getAwsAccessKeyId())
                && StringUtils.hasText(properties.getAwsSecretAccessKey())) {
            clientBuilder.filter(new AmazonSpApiSigningFilter(properties));
        }

        return clientBuilder.build();
    }
}
