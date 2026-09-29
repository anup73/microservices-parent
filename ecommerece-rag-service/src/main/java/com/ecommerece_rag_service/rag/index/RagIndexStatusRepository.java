package com.ecommerece_rag_service.rag.index;

import com.ecommerece_rag_service.config.SqliteVecConfiguration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Tracks which entities have already been embedded and with which content hash, so re-indexing
 * only re-embeds documents whose text actually changed (avoids unnecessary embedding API calls).
 */
@Repository
public class RagIndexStatusRepository {

	private final JdbcTemplate jdbcTemplate;

	public RagIndexStatusRepository(
			@Qualifier(SqliteVecConfiguration.QUALIFIER) JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Optional<String> findContentHash(String entityType, String entityId) {
		try {
			String hash = jdbcTemplate.queryForObject(
					"SELECT content_hash FROM rag_index_status WHERE entity_type = ? AND entity_id = ?",
					String.class,
					entityType,
					entityId);
			return Optional.ofNullable(hash);
		} catch (EmptyResultDataAccessException exception) {
			return Optional.empty();
		}
	}

	public Set<String> findIndexedEntityIds(String entityType) {
		List<String> ids = jdbcTemplate.query(
				"SELECT entity_id FROM rag_index_status WHERE entity_type = ?",
				(resultSet, rowNum) -> resultSet.getString("entity_id"),
				entityType);
		return new HashSet<>(ids);
	}

	public void upsert(String entityType, String entityId, String contentHash) {
		jdbcTemplate.update("DELETE FROM rag_index_status WHERE entity_type = ? AND entity_id = ?",
				entityType, entityId);
		jdbcTemplate.update(
				"INSERT INTO rag_index_status (entity_type, entity_id, content_hash, indexed_at) VALUES (?, ?, ?, ?)",
				entityType,
				entityId,
				contentHash,
				Instant.now().toString());
	}

	public void delete(String entityType, String entityId) {
		jdbcTemplate.update("DELETE FROM rag_index_status WHERE entity_type = ? AND entity_id = ?",
				entityType, entityId);
	}
}
