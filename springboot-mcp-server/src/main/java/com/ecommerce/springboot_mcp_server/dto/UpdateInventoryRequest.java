package com.ecommerce.springboot_mcp_server.dto;

public record UpdateInventoryRequest(
        Integer quantityAvailable,
        Integer quantityReserved
) {
}
