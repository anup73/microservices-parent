package com.ecommerce.springboot_mcp_server.dto;

import java.util.List;

public record UpdateProductImagesRequest(
        List<CreateProductImageRequest> images
) {
}
