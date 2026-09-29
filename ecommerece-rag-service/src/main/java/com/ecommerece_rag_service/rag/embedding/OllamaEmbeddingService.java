package com.ecommerece_rag_service.rag.embedding;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Calls Ollama's {@code POST /api/embed} endpoint. Requests are split into batches to keep
 * payloads manageable during bulk indexing; response order matches the input order.
 */
@Service
@EnableConfigurationProperties(OllamaEmbeddingProperties.class)
public class OllamaEmbeddingService implements EmbeddingService {

	private static final Logger log = LoggerFactory.getLogger(OllamaEmbeddingService.class);

	private final RestClient restClient;
	private final OllamaEmbeddingProperties properties;

	public OllamaEmbeddingService(OllamaEmbeddingProperties properties) {
		this.properties = properties;
		String apiKey = resolveApiKey(properties);
		RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
		if (StringUtils.hasText(apiKey)) {
			builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim());
		}
		this.restClient = builder.build();
	}

	@Override
	public float[] embed(String text) {
		log.info("Embedding single text (length={} chars) via Ollama", text == null ? 0 : text.length());
		List<float[]> result = embedBatch(List.of(text));
		return result.get(0);
	}

	@Override
	public List<float[]> embedBatch(List<String> texts) {
		if (CollectionUtils.isEmpty(texts)) {
			return List.of();
		}
		if (!StringUtils.hasText(properties.getModel())) {
			throw new IllegalStateException("app.rag.ollama.model must be configured to generate embeddings.");
		}

		int batchSize = Math.max(1, properties.getBatchSize());
		log.info(
				"Embedding batch of {} text(s) via Ollama model={} baseUrl={} batchSize={}",
				texts.size(),
				properties.getModel(),
				properties.getBaseUrl(),
				batchSize);

		List<float[]> embeddings = new ArrayList<>(texts.size());
		for (int start = 0; start < texts.size(); start += batchSize) {
			List<String> batch = texts.subList(start, Math.min(start + batchSize, texts.size()));
			log.info(
					"Calling Ollama POST /api/embed for texts {}-{} of {}",
					start + 1,
					start + batch.size(),
					texts.size());
			embeddings.addAll(embedSingleBatch(batch));
		}
		log.info(
				"Ollama embedding complete — produced {} vector(s), dimensions={}",
				embeddings.size(),
				embeddings.isEmpty() ? 0 : embeddings.get(0).length);
		return embeddings;
	}

	private List<float[]> embedSingleBatch(List<String> batch) {
		EmbedResponse response;
		try {
			response = restClient
					.post()
					.uri("/api/embed")
					.contentType(MediaType.APPLICATION_JSON)
					.body(new EmbedRequest(properties.getModel(), batch))
					.retrieve()
					.body(EmbedResponse.class);
		} catch (HttpClientErrorException.Unauthorized exception) {
			throw new IllegalStateException(
					"Ollama returned 401 for model '"
							+ properties.getModel()
							+ "'. Local Ollama needs no auth for local models, but cloud models "
							+ "(e.g. *-cloud) require either `ollama signin` or "
							+ "app.rag.ollama.api-key / APP_RAG_OLLAMA_API_KEY / OLLAMA_API_KEY.",
					exception);
		}

		if (response == null || CollectionUtils.isEmpty(response.embeddings())) {
			throw new IllegalStateException("Ollama /api/embed returned an empty response.");
		}
		if (response.embeddings().size() != batch.size()) {
			throw new IllegalStateException(
					"Ollama /api/embed returned "
							+ response.embeddings().size()
							+ " embeddings for "
							+ batch.size()
							+ " inputs.");
		}

		return response.embeddings().stream().map(this::toFloatArray).toList();
	}

	private static String resolveApiKey(OllamaEmbeddingProperties properties) {
		if (StringUtils.hasText(properties.getApiKey())) {
			return properties.getApiKey();
		}
		String fromAppEnv = System.getenv("APP_RAG_OLLAMA_API_KEY");
		if (StringUtils.hasText(fromAppEnv)) {
			return fromAppEnv;
		}
		return System.getenv("OLLAMA_API_KEY");
	}

	private float[] toFloatArray(List<Double> values) {
		float[] embedding = new float[values.size()];
		for (int i = 0; i < values.size(); i++) {
			embedding[i] = values.get(i).floatValue();
		}
		return embedding;
	}

	private record EmbedRequest(String model, List<String> input) {
	}

	private record EmbedResponse(List<List<Double>> embeddings) {
	}
}
