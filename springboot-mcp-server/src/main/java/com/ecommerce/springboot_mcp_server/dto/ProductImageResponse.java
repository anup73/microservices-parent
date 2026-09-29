package com.ecommerce.springboot_mcp_server.dto;

public record ProductImageResponse(
        Long productImageId,
        String imageUrl,
        Integer displayOrder,
        Boolean isPrimary
) {
}
