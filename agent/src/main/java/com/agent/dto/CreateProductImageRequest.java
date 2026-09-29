package com.agent.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProductImageRequest(
        @NotBlank(message = "Image URL is required")
        String imageUrl,
        Integer displayOrder,
        Boolean primary
) {
}
