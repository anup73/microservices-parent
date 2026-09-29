package com.ecommerece_rag_service.rag.index;

/**
 * Summary of a {@link RagIndexer} run.
 */
public record IndexingResult(
		int categoriesTotal,
		int categoriesIndexed,
		int categoriesSkippedUnchanged,
		int productsTotal,
		int productsIndexed,
		int productsSkippedUnchanged,
		int staleDocumentsRemoved) {
}
