package com.ecommerce.springboot_mcp_server.dto;

import java.time.LocalDateTime;

public record InventoryResponse(
        Long inventoryId,
        Long productId,
        String sku,
        String productName,
        Integer quantityAvailable,
        Integer quantityReserved,
        LocalDateTime updatedAt
) {
}
