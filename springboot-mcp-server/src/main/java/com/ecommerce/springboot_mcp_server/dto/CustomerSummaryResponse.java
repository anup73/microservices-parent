package com.ecommerce.springboot_mcp_server.dto;

public record CustomerSummaryResponse(
        Long customerId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String status
) {
}
