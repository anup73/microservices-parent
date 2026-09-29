package com.ecommerce.springboot_mcp_server.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerDetailsResponse(
        Long customerId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CustomerAddressResponse> addresses
) {
}
