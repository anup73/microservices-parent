package com.agent.controller;

import com.agent.dto.AmazonAccessTokenResponse;
import com.agent.dto.SearchOrdersRequest;
import com.agent.service.AmazonLwaService;
import com.agent.service.AmazonOrdersService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@Profile("amazon")
@RequestMapping("/api/amazon")
public class AmazonOrdersController {

    private final AmazonLwaService amazonLwaService;
    private final AmazonOrdersService amazonOrdersService;

    public AmazonOrdersController(AmazonLwaService amazonLwaService, AmazonOrdersService amazonOrdersService) {
        this.amazonLwaService = amazonLwaService;
        this.amazonOrdersService = amazonOrdersService;
    }

    @GetMapping("/token")
    public Mono<AmazonAccessTokenResponse> getAccessToken() {
        return amazonLwaService.getAccessToken();
    }

    @GetMapping("/orders")
    public Mono<Map<String, Object>> searchOrders(@Valid @ModelAttribute SearchOrdersRequest request) {
        return amazonOrdersService.searchOrders(request);
    }

    @GetMapping("/orders/{orderId}")
    public Mono<Map<String, Object>> getOrder(
            @PathVariable String orderId,
            @RequestParam(required = false) List<String> includedData
    ) {
        return amazonOrdersService.getOrder(orderId, includedData);
    }
}
