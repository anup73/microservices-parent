package com.ecommerece_rag_service.rag.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.rag.ollama")
public class OllamaEmbeddingProperties {

	private String baseUrl = "http://localhost:11434";
	private String model = "gemma4:31b-cloud";
	/**
	 * Optional. Required for Ollama Cloud models (names ending in {@code -cloud}) when the local
	 * daemon is not signed in. Prefer env var {@code APP_RAG_OLLAMA_API_KEY} or
	 * {@code OLLAMA_API_KEY} over committing a key into properties.
	 */
	private String apiKey;
	private int batchSize = 32;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
}
