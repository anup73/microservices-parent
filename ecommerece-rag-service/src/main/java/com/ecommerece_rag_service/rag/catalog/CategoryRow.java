package com.ecommerece_rag_service.rag.catalog;

/**
 * Read-only projection of the {@code category} table.
 */
public record CategoryRow(
		Long categoryId,
		String name,
		String description,
		Long parentCategoryId,
		String status) {
}
