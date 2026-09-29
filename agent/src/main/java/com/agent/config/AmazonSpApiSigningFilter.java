package com.agent.config;

import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4FamilyHttpSigner;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignRequest;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AmazonSpApiSigningFilter implements ExchangeFilterFunction {

    private static final String SERVICE_NAME = "execute-api";

    private final AmazonSpApiProperties properties;
    private final StaticCredentialsProvider credentialsProvider;
    private final AwsV4HttpSigner signer;

    public AmazonSpApiSigningFilter(AmazonSpApiProperties properties) {
        Assert.hasText(properties.getAwsAccessKeyId(), "amazon.sp-api.aws-access-key-id must not be blank");
        Assert.hasText(properties.getAwsSecretAccessKey(), "amazon.sp-api.aws-secret-access-key must not be blank");
        this.properties = properties;
        this.credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAwsAccessKeyId(), properties.getAwsSecretAccessKey())
        );
        this.signer = AwsV4HttpSigner.create();
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        ClientRequest signedRequest = ClientRequest.from(request)
                .headers(headers -> {
                    headers.clear();
                    copySignedHeaders(request, headers);
                })
                .build();
        return next.exchange(signedRequest);
    }

    private void copySignedHeaders(ClientRequest request, HttpHeaders targetHeaders) {
        SdkHttpFullRequest unsignedRequest = SdkHttpFullRequest.builder()
                .method(SdkHttpMethod.fromValue(request.method().name()))
                .uri(request.url())
                .headers(toSingleValueHeaders(request.headers()))
                .build();

        AwsCredentialsIdentity credentials = credentialsProvider.resolveCredentials();
        SdkHttpFullRequest signedRequest = (SdkHttpFullRequest) signer.sign(SignRequest.builder(credentials)
                        .request(unsignedRequest)
                        .putProperty(AwsV4FamilyHttpSigner.SERVICE_SIGNING_NAME, SERVICE_NAME)
                        .putProperty(AwsV4HttpSigner.REGION_NAME, properties.getRegion())
                        .build())
                .request();

        signedRequest.headers().forEach((name, values) -> targetHeaders.put(name, List.copyOf(values)));
    }

    private static Map<String, List<String>> toSingleValueHeaders(HttpHeaders headers) {
        return headers.headerSet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
