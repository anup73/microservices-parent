package com.ecommerece_rag_service.rag.catalog;

import java.math.BigDecimal;

/**
 * Read-only projection of the {@code product} table.
 */
public record ProductRow(
		Long productId,
		String sku,
		String name,
		String description,
		Long categoryId,
		BigDecimal price,
		String currency,
		String status) {
}
