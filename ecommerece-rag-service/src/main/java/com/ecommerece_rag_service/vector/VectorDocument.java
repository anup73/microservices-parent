package com.ecommerece_rag_service.vector;

/**
 * A document to be embedded and stored in the {@code rag_documents} vec0 table.
 *
 * @param documentId   stable external id, e.g. {@code "product-1001"} or {@code "category-25"}
 * @param entityType   {@code PRODUCT} or {@code CATEGORY}
 * @param sku          product SKU, null for categories
 * @param categoryId   category id associated with the document, may be null
 * @param categoryPath full category breadcrumb, e.g. {@code "Electronics > Audio > Headphones"}
 * @param status       lifecycle status, e.g. {@code ACTIVE}
 * @param currency     ISO currency code, product documents only
 * @param price        formatted price, product documents only
 * @param content      natural-language text that was embedded
 * @param embedding    the embedding vector, must match the configured dimensions
 */
public record VectorDocument(
		String documentId,
		String entityType,
		String sku,
		String categoryId,
		String categoryPath,
		String status,
		String currency,
		String price,
		String content,
		float[] embedding) {
}
