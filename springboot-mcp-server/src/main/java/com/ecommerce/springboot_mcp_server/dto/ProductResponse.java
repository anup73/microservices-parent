package com.ecommerce.springboot_mcp_server.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        Long productId,
        String sku,
        String name,
        String description,
        Long categoryId,
        String categoryName,
        BigDecimal price,
        String currency,
        String status,
        Integer quantityAvailable,
        Integer quantityReserved,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ProductImageResponse> images
) {
}
