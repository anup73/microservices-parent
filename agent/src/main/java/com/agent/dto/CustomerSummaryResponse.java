package com.agent.dto;

public record CustomerSummaryResponse(
        Long customerId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String status
) {
}
