package com.ecommerece_rag_service.rag.document;

import com.ecommerece_rag_service.rag.catalog.CategoryRow;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds the full category breadcrumb (e.g. {@code "Electronics > Audio > Headphones"}) from the
 * hierarchical {@code category} table, as recommended in the RAG design doc: embedding the full
 * path rather than just the leaf category name significantly improves retrieval quality.
 */
public class CategoryPathResolver {

	private static final String SEPARATOR = " > ";

	private final Map<Long, CategoryRow> categoriesById;

	public CategoryPathResolver(List<CategoryRow> categories) {
		this.categoriesById = categories.stream()
				.collect(Collectors.toMap(CategoryRow::categoryId, Function.identity()));
	}

	/**
	 * Returns the full breadcrumb path for the given category id, root-first. Returns an empty
	 * string if the category id is null or unknown. Guards against corrupt/cyclic parent chains.
	 */
	public String pathFor(Long categoryId) {
		if (categoryId == null) {
			return "";
		}

		Deque<String> segments = new ArrayDeque<>();
		Set<Long> visited = new HashSet<>();
		Long currentId = categoryId;

		while (currentId != null && visited.add(currentId)) {
			CategoryRow category = categoriesById.get(currentId);
			if (category == null) {
				break;
			}
			segments.addFirst(category.name());
			currentId = category.parentCategoryId();
		}

		return String.join(SEPARATOR, segments);
	}

	public CategoryRow findById(Long categoryId) {
		return categoryId == null ? null : categoriesById.get(categoryId);
	}
}
