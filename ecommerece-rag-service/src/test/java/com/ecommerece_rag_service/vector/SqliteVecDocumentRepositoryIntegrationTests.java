package com.ecommerece_rag_service.vector;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfSystemProperty(named = "app.sqlite-vec.extension-path", matches = ".+")
class SqliteVecDocumentRepositoryIntegrationTests {

	@Autowired
	private SqliteVecDocumentRepository repository;

	@Test
	void returnsNearestDocument() {
		float[] firstEmbedding = embedding(1.0f, 0.0f);
		float[] secondEmbedding = embedding(0.0f, 1.0f);

		repository.upsert(document("sqlite-vec-test-first", "First document", firstEmbedding));
		repository.upsert(document("sqlite-vec-test-second", "Second document", secondEmbedding));

		List<VectorSearchResult> results = repository.search(embedding(0.9f, 0.1f), 1);

		assertThat(results).singleElement().extracting(VectorSearchResult::documentId)
				.isEqualTo("sqlite-vec-test-first");
	}

	private VectorDocument document(String documentId, String content, float[] embedding) {
		return new VectorDocument(
				documentId, "PRODUCT", "SKU-1", "1", "Category", "ACTIVE", "INR", "0", content, embedding);
	}

	private float[] embedding(float first, float second) {
		float[] embedding = new float[768];
		Arrays.fill(embedding, 0.0f);
		embedding[0] = first;
		embedding[1] = second;
		return embedding;
	}
}
