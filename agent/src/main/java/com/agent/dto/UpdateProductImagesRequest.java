package com.agent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateProductImagesRequest(
        @NotEmpty(message = "At least one image is required")
        List<@Valid CreateProductImageRequest> images
) {
}
