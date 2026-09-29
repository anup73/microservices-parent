package com.ecommerece_rag_service.rag.web;

import com.ecommerece_rag_service.rag.index.IndexingResult;
import com.ecommerece_rag_service.rag.index.RagIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
public class RagIndexController {

	private static final Logger log = LoggerFactory.getLogger(RagIndexController.class);

	private final RagIndexer ragIndexer;

	public RagIndexController(RagIndexer ragIndexer) {
		this.ragIndexer = ragIndexer;
	}

	@PostMapping("/reindex")
	public IndexingResult reindex() {
		log.info("Received POST /api/rag/reindex — starting reindex flow");
		IndexingResult result = ragIndexer.reindexAll();
		log.info(
				"Completed POST /api/rag/reindex — categoriesTotal={}, categoriesIndexed={}, categoriesSkipped={}, productsTotal={}, productsIndexed={}, productsSkipped={}, staleRemoved={}",
				result.categoriesTotal(),
				result.categoriesIndexed(),
				result.categoriesSkippedUnchanged(),
				result.productsTotal(),
				result.productsIndexed(),
				result.productsSkippedUnchanged(),
				result.staleDocumentsRemoved());
		return result;
	}
}
