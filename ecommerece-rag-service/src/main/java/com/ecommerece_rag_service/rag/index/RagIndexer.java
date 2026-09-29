package com.ecommerece_rag_service.rag.index;

import com.ecommerece_rag_service.rag.catalog.CatalogRepository;
import com.ecommerece_rag_service.rag.catalog.CategoryRow;
import com.ecommerece_rag_service.rag.catalog.ProductRow;
import com.ecommerece_rag_service.rag.document.CategoryPathResolver;
import com.ecommerece_rag_service.rag.document.RagDocument;
import com.ecommerece_rag_service.rag.document.RagDocumentBuilder;
import com.ecommerece_rag_service.rag.embedding.EmbeddingService;
import com.ecommerece_rag_service.vector.SqliteVecDocumentRepository;
import com.ecommerece_rag_service.vector.VectorDocument;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Orchestrates the ingestion pipeline described in the design doc:
 *
 * <pre>
 * MySQL -&gt; extract categories/products -&gt; resolve category paths -&gt; build documents
 *       -&gt; skip unchanged (content hash) -&gt; embed changed documents -&gt; upsert into sqlite-vec
 * </pre>
 */
@Service
public class RagIndexer {

	private static final Logger log = LoggerFactory.getLogger(RagIndexer.class);

	private static final String PRODUCT = "PRODUCT";
	private static final String CATEGORY = "CATEGORY";

	private final CatalogRepository catalogRepository;
	private final RagDocumentBuilder documentBuilder;
	private final EmbeddingService embeddingService;
	private final SqliteVecDocumentRepository vectorRepository;
	private final RagIndexStatusRepository indexStatusRepository;

	public RagIndexer(
			CatalogRepository catalogRepository,
			RagDocumentBuilder documentBuilder,
			EmbeddingService embeddingService,
			SqliteVecDocumentRepository vectorRepository,
			RagIndexStatusRepository indexStatusRepository) {
		this.catalogRepository = catalogRepository;
		this.documentBuilder = documentBuilder;
		this.embeddingService = embeddingService;
		this.vectorRepository = vectorRepository;
		this.indexStatusRepository = indexStatusRepository;
	}

	public IndexingResult reindexAll() {
		log.info("Reindex step 1/5 — loading categories and products from MySQL");
		List<CategoryRow> categories = catalogRepository.findAllCategories();
		List<ProductRow> products = catalogRepository.findAllProducts();
		log.info(
				"Reindex step 1/5 complete — loaded {} categories and {} products from MySQL",
				categories.size(),
				products.size());

		log.info("Reindex step 2/5 — building RAG documents with category paths");
		CategoryPathResolver categoryPathResolver = new CategoryPathResolver(categories);
		List<RagDocument> categoryDocuments = categories.stream()
				.map(category -> documentBuilder.buildCategoryDocument(category, categoryPathResolver))
				.toList();
		List<RagDocument> productDocuments = products.stream()
				.map(product -> documentBuilder.buildProductDocument(product, categoryPathResolver))
				.toList();
		log.info(
				"Reindex step 2/5 complete — built {} category documents and {} product documents",
				categoryDocuments.size(),
				productDocuments.size());

		log.info("Reindex step 3/5 — indexing changed category documents");
		IndexingOutcome categoryOutcome = indexChangedDocuments(CATEGORY, categoryDocuments);
		log.info(
				"Reindex step 3/5 complete — categories indexed={}, skippedUnchanged={}",
				categoryOutcome.indexed(),
				categoryOutcome.skipped());

		log.info("Reindex step 4/5 — indexing changed product documents");
		IndexingOutcome productOutcome = indexChangedDocuments(PRODUCT, productDocuments);
		log.info(
				"Reindex step 4/5 complete — products indexed={}, skippedUnchanged={}",
				productOutcome.indexed(),
				productOutcome.skipped());

		log.info("Reindex step 5/5 — removing stale documents no longer present in MySQL");
		int staleRemoved =
				removeStaleDocuments(CATEGORY, categories.stream().map(c -> c.categoryId().toString()).toList())
						+ removeStaleDocuments(PRODUCT, products.stream().map(p -> p.productId().toString()).toList());
		log.info("Reindex step 5/5 complete — staleDocumentsRemoved={}", staleRemoved);

		IndexingResult result = new IndexingResult(
				categoryDocuments.size(),
				categoryOutcome.indexed(),
				categoryOutcome.skipped(),
				productDocuments.size(),
				productOutcome.indexed(),
				productOutcome.skipped(),
				staleRemoved);
		log.info("Reindex pipeline finished — {}", result);
		return result;
	}

	private IndexingOutcome indexChangedDocuments(String entityType, List<RagDocument> documents) {
		List<RagDocument> changed = new ArrayList<>();
		List<String> changedHashes = new ArrayList<>();

		log.info(
				"Comparing content hashes for {} {} document(s) to detect changes",
				documents.size(),
				entityType);
		for (RagDocument document : documents) {
			String hash = sha256(document.content());
			boolean unchanged = indexStatusRepository
					.findContentHash(entityType, entityId(document))
					.map(existingHash -> existingHash.equals(hash))
					.orElse(false);
			if (!unchanged) {
				changed.add(document);
				changedHashes.add(hash);
			}
		}

		if (changed.isEmpty()) {
			log.info("No {} documents changed — skipping embed and vector upsert", entityType);
			return new IndexingOutcome(0, documents.size());
		}

		log.info(
				"{} {} document(s) changed — embedding and upserting into vector DB",
				changed.size(),
				entityType);
		for (RagDocument document : changed) {
			log.info(
					"RAG document ready to embed and store: documentId={}, entityType={}, sku={}, categoryId={}, categoryPath={}, status={}, currency={}, price={}, content=\n{}",
					document.documentId(),
					document.entityType(),
					document.sku(),
					document.categoryId(),
					document.categoryPath(),
					document.status(),
					document.currency(),
					document.price(),
					document.content());
		}

		log.info("Calling EmbeddingService.embedBatch for {} {} document(s)", changed.size(), entityType);
		List<float[]> embeddings = embeddingService.embedBatch(changed.stream().map(RagDocument::content).toList());
		log.info("Received {} embedding(s) from EmbeddingService", embeddings.size());

		for (int i = 0; i < changed.size(); i++) {
			RagDocument document = changed.get(i);
			float[] embedding = embeddings.get(i);
			log.info("Upserting documentId={} into sqlite-vec", document.documentId());
			vectorRepository.upsert(new VectorDocument(
					document.documentId(),
					document.entityType(),
					document.sku(),
					document.categoryId(),
					document.categoryPath(),
					document.status(),
					document.currency(),
					document.price(),
					document.content(),
					embedding));
			indexStatusRepository.upsert(entityType, entityId(document), changedHashes.get(i));
			log.info(
					"Successfully inserted into vector DB: documentId={}, entityType={}, embeddingDimensions={}",
					document.documentId(),
					document.entityType(),
					embedding.length);
		}

		return new IndexingOutcome(changed.size(), documents.size() - changed.size());
	}

	private int removeStaleDocuments(String entityType, List<String> currentEntityIds) {
		Set<String> current = new HashSet<>(currentEntityIds);
		Set<String> indexed = indexStatusRepository.findIndexedEntityIds(entityType);
		int removed = 0;
		for (String entityId : indexed) {
			if (!current.contains(entityId)) {
				String docId = documentId(entityType, entityId);
				log.info("Removing stale {} document from vector DB: documentId={}", entityType, docId);
				vectorRepository.delete(docId);
				indexStatusRepository.delete(entityType, entityId);
				removed++;
			}
		}
		return removed;
	}

	private String entityId(RagDocument document) {
		return document.documentId().substring(document.documentId().indexOf('-') + 1);
	}

	private String documentId(String entityType, String entityId) {
		return (CATEGORY.equals(entityType) ? "category-" : "product-") + entityId;
	}

	private String sha256(String text) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available.", exception);
		}
	}

	private record IndexingOutcome(int indexed, int skipped) {
	}
}
