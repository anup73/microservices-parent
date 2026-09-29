package com.agent.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "amazon.sp-api")
public class AmazonSpApiProperties {

    @NotBlank
    private String endpoint;

    @NotBlank
    private String region;

    @NotBlank
    private String marketplaceId;

    @NotBlank
    private String lwaClientId;

    @NotBlank
    private String lwaClientSecret;

    @NotBlank
    private String lwaRefreshToken;

    private String awsAccessKeyId;

    private String awsSecretAccessKey;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getMarketplaceId() {
        return marketplaceId;
    }

    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    public String getLwaClientId() {
        return lwaClientId;
    }

    public void setLwaClientId(String lwaClientId) {
        this.lwaClientId = lwaClientId;
    }

    public String getLwaClientSecret() {
        return lwaClientSecret;
    }

    public void setLwaClientSecret(String lwaClientSecret) {
        this.lwaClientSecret = lwaClientSecret;
    }

    public String getLwaRefreshToken() {
        return lwaRefreshToken;
    }

    public void setLwaRefreshToken(String lwaRefreshToken) {
        this.lwaRefreshToken = lwaRefreshToken;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }
}
