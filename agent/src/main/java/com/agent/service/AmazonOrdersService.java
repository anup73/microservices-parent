package com.agent.service;

import com.agent.dto.SearchOrdersRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Profile("amazon")
public class AmazonOrdersService {

    private static final Logger log = LoggerFactory.getLogger(AmazonOrdersService.class);

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_RESPONSE =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient amazonSpApiWebClient;
    private final AmazonLwaService amazonLwaService;

    public AmazonOrdersService(WebClient amazonSpApiWebClient, AmazonLwaService amazonLwaService) {
        this.amazonSpApiWebClient = amazonSpApiWebClient;
        this.amazonLwaService = amazonLwaService;
    }

    public Mono<Map<String, Object>> searchOrders(SearchOrdersRequest request) {
        log.info("Searching Amazon orders");
        return amazonLwaService.getAccessToken()
                .flatMap(accessToken -> amazonSpApiWebClient.get()
                        .uri(uriBuilder -> {
                            uriBuilder.path("/orders/2026-01-01/orders");
                            addValue(uriBuilder, "createdAfter", request.getCreatedAfter() == null ? null : request.getCreatedAfter().toString());
                            addValue(uriBuilder, "createdBefore", request.getCreatedBefore() == null ? null : request.getCreatedBefore().toString());
                            addValue(uriBuilder, "lastUpdatedAfter", request.getLastUpdatedAfter() == null ? null : request.getLastUpdatedAfter().toString());
                            addValue(uriBuilder, "lastUpdatedBefore", request.getLastUpdatedBefore() == null ? null : request.getLastUpdatedBefore().toString());
                            addValues(uriBuilder, "fulfillmentStatuses", request.getFulfillmentStatuses());
                            addValues(uriBuilder, "marketplaceIds", request.getMarketplaceIds());
                            addValues(uriBuilder, "fulfilledBy", request.getFulfilledBy());
                            addValue(uriBuilder, "maxResultsPerPage", request.getMaxResultsPerPage());
                            addValue(uriBuilder, "paginationToken", request.getPaginationToken());
                            addValues(uriBuilder, "includedData", request.getIncludedData());
                            return uriBuilder.build();
                        })
                        .header("x-amz-access-token", accessToken.accessToken())
                        .retrieve()
                        .bodyToMono(MAP_RESPONSE))
                .doOnError(error -> log.error("Amazon order search failed: {}", error.getMessage()));
    }

    public Mono<Map<String, Object>> getOrder(String orderId, List<String> includedData) {
        log.info("Fetching Amazon order {}", orderId);
        return amazonLwaService.getAccessToken()
                .flatMap(accessToken -> amazonSpApiWebClient.get()
                        .uri(uriBuilder -> {
                            uriBuilder.path("/orders/2026-01-01/orders/{orderId}");
                            addValues(uriBuilder, "includedData", includedData);
                            return uriBuilder.build(orderId);
                        })
                        .header("x-amz-access-token", accessToken.accessToken())
                        .retrieve()
                        .bodyToMono(MAP_RESPONSE))
                .doOnError(error -> log.error("Amazon order {} fetch failed: {}", orderId, error.getMessage()));
    }

    private static void addValue(org.springframework.web.util.UriBuilder uriBuilder, String name, Object value) {
        if (value != null) {
            uriBuilder.queryParam(name, value);
        }
    }

    private static void addValues(org.springframework.web.util.UriBuilder uriBuilder, String name, List<String> values) {
        if (values != null && !values.isEmpty()) {
            uriBuilder.queryParam(name, values.toArray());
        }
    }
}
