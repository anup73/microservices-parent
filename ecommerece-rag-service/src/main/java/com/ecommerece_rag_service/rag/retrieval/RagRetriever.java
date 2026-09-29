package com.ecommerece_rag_service.rag.retrieval;

import com.ecommerece_rag_service.rag.embedding.EmbeddingService;
import com.ecommerece_rag_service.vector.SqliteVecDocumentRepository;
import com.ecommerece_rag_service.vector.VectorSearchResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Semantic retrieval over the sqlite-vec store. This only performs step 1 (semantic search) of
 * the hybrid retrieval design; callers needing live price/availability must follow up with a
 * structured MySQL query using the returned {@code sku}/{@code documentId}.
 */
@Service
public class RagRetriever {

	private static final Logger log = LoggerFactory.getLogger(RagRetriever.class);

	private final EmbeddingService embeddingService;
	private final SqliteVecDocumentRepository vectorRepository;

	public RagRetriever(EmbeddingService embeddingService, SqliteVecDocumentRepository vectorRepository) {
		this.embeddingService = embeddingService;
		this.vectorRepository = vectorRepository;
	}

	public List<RagSearchResult> search(String query, int limit) {
		log.info("Search step 1/3 — embedding query via EmbeddingService");
		float[] queryEmbedding = embeddingService.embed(query);
		log.info(
				"Search step 1/3 complete — query embedding dimensions={}",
				queryEmbedding.length);

		log.info("Search step 2/3 — running nearest-neighbor search in sqlite-vec (limit={})", limit);
		List<VectorSearchResult> hits = vectorRepository.search(queryEmbedding, limit);
		log.info("Search step 2/3 complete — vector DB returned {} hit(s)", hits.size());

		log.info("Search step 3/3 — mapping vector hits to RagSearchResult");
		List<RagSearchResult> results = hits.stream().map(this::toSearchResult).toList();
		for (RagSearchResult result : results) {
			log.info(
					"Search hit: documentId={}, entityType={}, sku={}, distance={}, categoryPath={}",
					result.documentId(),
					result.entityType(),
					result.sku(),
					result.distance(),
					result.categoryPath());
		}
		log.info("Search pipeline finished — returning {} result(s)", results.size());
		return results;
	}

	private RagSearchResult toSearchResult(VectorSearchResult result) {
		return new RagSearchResult(
				result.documentId(),
				result.entityType(),
				result.sku(),
				result.categoryId(),
				result.categoryPath(),
				result.status(),
				result.currency(),
				result.price(),
				result.content(),
				result.distance());
	}
}
