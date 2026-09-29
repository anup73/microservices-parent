package com.agent.dto;

public record ProductImageResponse(
        Long productImageId,
        String imageUrl,
        Integer displayOrder,
        Boolean isPrimary
) {
}
