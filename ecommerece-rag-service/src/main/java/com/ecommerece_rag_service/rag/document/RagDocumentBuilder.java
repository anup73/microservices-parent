package com.ecommerece_rag_service.rag.document;

import com.ecommerece_rag_service.rag.catalog.CategoryRow;
import com.ecommerece_rag_service.rag.catalog.ProductRow;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Builds the natural-language "knowledge documents" that get embedded, following the design doc:
 * one coherent paragraph per product/category rather than a flat dump of column values, with the
 * full category breadcrumb included for better semantic context.
 */
@Component
public class RagDocumentBuilder {

	public RagDocument buildProductDocument(
			ProductRow product, CategoryPathResolver categoryPathResolver) {
		String categoryPath = categoryPathResolver.pathFor(product.categoryId());
		String description = StringUtils.hasText(product.description())
				? product.description().trim()
				: "No further description is available.";

		StringBuilder content = new StringBuilder();
		content.append("Product: ").append(product.name()).append('\n');
		if (StringUtils.hasText(categoryPath)) {
			content.append("Category: ").append(categoryPath).append('\n');
		}
		content.append('\n');
		content.append("The ").append(product.name());
		if (StringUtils.hasText(categoryPath)) {
			content.append(" is part of the ").append(categoryPath).append(" category.");
		} else {
			content.append(" is not currently assigned to a category.");
		}
		content.append('\n');
		content.append("It has SKU ").append(product.sku()).append(".\n");
		content.append(description).append('\n');
		content.append("The product is currently ").append(product.status()).append(".\n");
		content.append("Price: ").append(product.currency()).append(' ').append(product.price()).append('.');

		return new RagDocument(
				"product-" + product.productId(),
				"PRODUCT",
				product.sku(),
				product.categoryId() == null ? null : product.categoryId().toString(),
				categoryPath,
				product.status(),
				product.currency(),
				product.price().toPlainString(),
				content.toString());
	}

	public RagDocument buildCategoryDocument(
			CategoryRow category, CategoryPathResolver categoryPathResolver) {
		String categoryPath = categoryPathResolver.pathFor(category.categoryId());
		String description = StringUtils.hasText(category.description())
				? category.description().trim()
				: "No further description is available.";

		StringBuilder content = new StringBuilder();
		content.append("Category: ").append(category.name()).append('\n');
		content.append("Path: ").append(categoryPath).append('\n');
		content.append('\n');
		content.append(description).append('\n');
		content.append("This category is currently ").append(category.status()).append('.');

		return new RagDocument(
				"category-" + category.categoryId(),
				"CATEGORY",
				null,
				category.categoryId().toString(),
				categoryPath,
				category.status(),
				null,
				null,
				content.toString());
	}
}
