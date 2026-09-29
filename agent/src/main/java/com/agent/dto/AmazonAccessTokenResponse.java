package com.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AmazonAccessTokenResponse(
        @JsonProperty("access_token")
        @NotBlank
        String accessToken,
        @JsonProperty("token_type")
        @NotBlank
        String tokenType,
        @JsonProperty("expires_in")
        Long expiresIn
) {
}
