package com.ecommerece_rag_service.vector;

public record VectorSearchResult(
		String documentId,
		String entityType,
		String sku,
		String categoryId,
		String categoryPath,
		String status,
		String currency,
		String price,
		String content,
		double distance) {
}
