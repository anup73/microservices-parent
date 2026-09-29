package com.ecommerece_rag_service.rag.retrieval;

/**
 * A semantic search hit returned to callers. {@code productId}/{@code categoryId} let the caller
 * go back to MySQL for live/structured data (price, stock, etc.) as recommended by the hybrid
 * RAG design: vector search identifies *which* entities are relevant, MySQL remains the source
 * of truth for anything that changes frequently.
 */
public record RagSearchResult(
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
