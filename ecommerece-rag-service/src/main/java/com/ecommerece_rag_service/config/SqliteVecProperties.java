package com.ecommerece_rag_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.sqlite-vec")
public class SqliteVecProperties {

	private String databaseUrl = "jdbc:sqlite:./data/ecommerce-rag.db";
	private boolean enabled = true;
	private String extensionPath;
	private int dimensions = 768;

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getExtensionPath() {
		return extensionPath;
	}

	public void setExtensionPath(String extensionPath) {
		this.extensionPath = extensionPath;
	}

	public int getDimensions() {
		return dimensions;
	}

	public void setDimensions(int dimensions) {
		this.dimensions = dimensions;
	}
}
