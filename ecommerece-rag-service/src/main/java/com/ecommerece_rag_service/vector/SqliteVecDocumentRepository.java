package com.ecommerece_rag_service.vector;

import com.ecommerece_rag_service.config.SqliteVecConfiguration;
import com.ecommerece_rag_service.config.SqliteVecProperties;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class SqliteVecDocumentRepository {

	private final JdbcTemplate jdbcTemplate;
	private final SqliteVecProperties properties;

	public SqliteVecDocumentRepository(
			@Qualifier(SqliteVecConfiguration.QUALIFIER) JdbcTemplate jdbcTemplate,
			SqliteVecProperties properties) {
		this.jdbcTemplate = jdbcTemplate;
		this.properties = properties;
	}

	@Transactional
	public void upsert(VectorDocument document) {
		requireEnabled();
		validateEmbedding(document.embedding());
		jdbcTemplate.update("DELETE FROM rag_documents WHERE document_id = ?", document.documentId());
		jdbcTemplate.update(
				"""
						INSERT INTO rag_documents
						    (document_id, entity_type, sku, category_id, category_path, status, currency, price, content, embedding)
						VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
						""",
				document.documentId(),
				document.entityType(),
				document.sku(),
				document.categoryId(),
				document.categoryPath(),
				document.status(),
				document.currency(),
				document.price(),
				document.content(),
				toJson(document.embedding()));
	}

	@Transactional
	public void delete(String documentId) {
		requireEnabled();
		jdbcTemplate.update("DELETE FROM rag_documents WHERE document_id = ?", documentId);
	}

	public List<VectorSearchResult> search(float[] queryEmbedding, int limit) {
		requireEnabled();
		validateEmbedding(queryEmbedding);
		if (limit < 1) {
			throw new IllegalArgumentException("limit must be greater than zero.");
		}

		return jdbcTemplate.query(
				"""
						SELECT document_id, entity_type, sku, category_id, category_path, status, currency, price, content, distance
						FROM rag_documents
						WHERE embedding MATCH ? AND k = ?
						ORDER BY distance
						""",
				(resultSet, rowNum) -> new VectorSearchResult(
						resultSet.getString("document_id"),
						resultSet.getString("entity_type"),
						resultSet.getString("sku"),
						resultSet.getString("category_id"),
						resultSet.getString("category_path"),
						resultSet.getString("status"),
						resultSet.getString("currency"),
						resultSet.getString("price"),
						resultSet.getString("content"),
						resultSet.getDouble("distance")),
				toJson(queryEmbedding),
				limit);
	}

	private void requireEnabled() {
		if (!properties.isEnabled()) {
			throw new IllegalStateException("sqlite-vec is disabled.");
		}
	}

	private void validateEmbedding(float[] embedding) {
		if (embedding == null || embedding.length != properties.getDimensions()) {
			throw new IllegalArgumentException(
					"Embedding must contain exactly " + properties.getDimensions() + " dimensions.");
		}
		for (float value : embedding) {
			if (!Float.isFinite(value)) {
				throw new IllegalArgumentException("Embedding values must be finite.");
			}
		}
	}

	private String toJson(float[] embedding) {
		StringBuilder json = new StringBuilder(embedding.length * 10).append('[');
		for (int index = 0; index < embedding.length; index++) {
			if (index > 0) {
				json.append(',');
			}
			json.append(embedding[index]);
		}
		return json.append(']').toString();
	}
}
