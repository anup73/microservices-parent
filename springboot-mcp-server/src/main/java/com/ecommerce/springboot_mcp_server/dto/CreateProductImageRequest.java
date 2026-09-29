package com.ecommerce.springboot_mcp_server.dto;

public record CreateProductImageRequest(
        String imageUrl,
        Integer displayOrder,
        Boolean primary
) {
}
