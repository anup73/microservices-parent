package com.ecommerece_rag_service.rag.catalog;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Extracts the descriptive/static catalog data (category, product) from MySQL, which is the
 * source of truth. Only fields useful for building semantic RAG documents are read here;
 * highly dynamic data (inventory, live pricing) intentionally stays out of this repository and
 * must be queried live from MySQL at answer time.
 */
@Repository
public class CatalogRepository {

	private final JdbcTemplate jdbcTemplate;

	// Uses the primary (MySQL) JdbcTemplate/DataSource - no qualifier needed.
	public CatalogRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<CategoryRow> findAllCategories() {
		return jdbcTemplate.query(
				"""
						SELECT category_id, name, description, parent_category_id, status
						FROM category
						""",
				(resultSet, rowNum) -> new CategoryRow(
						resultSet.getLong("category_id"),
						resultSet.getString("name"),
						resultSet.getString("description"),
						(Long) resultSet.getObject("parent_category_id"),
						resultSet.getString("status")));
	}

	public List<ProductRow> findAllProducts() {
		return jdbcTemplate.query(
				"""
						SELECT product_id, sku, name, description, category_id, price, currency, status
						FROM product
						""",
				(resultSet, rowNum) -> new ProductRow(
						resultSet.getLong("product_id"),
						resultSet.getString("sku"),
						resultSet.getString("name"),
						resultSet.getString("description"),
						(Long) resultSet.getObject("category_id"),
						resultSet.getBigDecimal("price"),
						resultSet.getString("currency"),
						resultSet.getString("status")));
	}
}
