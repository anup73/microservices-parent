package com.ecommerece_rag_service.rag.document;

/**
 * A natural-language document ready to be embedded, plus the structured metadata that should be
 * stored alongside it (not embedded) for filtering/display purposes.
 */
public record RagDocument(
		String documentId,
		String entityType,
		String sku,
		String categoryId,
		String categoryPath,
		String status,
		String currency,
		String price,
		String content) {
}
