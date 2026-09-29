package com.ecommerce.springboot_mcp_server.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest(
        String sku,
        String name,
        String description,
        Long categoryId,
        BigDecimal price,
        String currency,
        String status,
        Integer quantityAvailable,
        Integer quantityReserved,
        List<CreateProductImageRequest> images
) {
}
