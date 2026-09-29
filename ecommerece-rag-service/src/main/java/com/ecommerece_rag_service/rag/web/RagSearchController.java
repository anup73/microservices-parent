package com.ecommerece_rag_service.rag.web;

import com.ecommerece_rag_service.rag.retrieval.RagRetriever;
import com.ecommerece_rag_service.rag.retrieval.RagSearchResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
public class RagSearchController {

	private static final Logger log = LoggerFactory.getLogger(RagSearchController.class);

	private final RagRetriever ragRetriever;

	public RagSearchController(RagRetriever ragRetriever) {
		this.ragRetriever = ragRetriever;
	}

	@GetMapping("/search")
	public List<RagSearchResult> search(
			@RequestParam("q") String query,
			@RequestParam(name = "limit", defaultValue = "10") int limit) {
		log.info("Received GET /api/rag/search — query='{}', limit={}", query, limit);
		List<RagSearchResult> results = ragRetriever.search(query, limit);
		log.info("Completed GET /api/rag/search — returned {} result(s)", results.size());
		return results;
	}
}
