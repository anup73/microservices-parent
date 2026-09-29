package com.ecommerece_rag_service.rag.embedding;

import java.util.List;

/**
 * Generates embedding vectors for text. Backed by Ollama's embeddings API by default; swap the
 * implementation to point at a different provider without touching ingestion or retrieval code.
 */
public interface EmbeddingService {

	float[] embed(String text);

	/**
	 * Embeds multiple texts, preserving input order in the result. Implementations should batch
	 * requests where the provider allows it to reduce API round-trips during bulk indexing.
	 */
	List<float[]> embedBatch(List<String> texts);
}
