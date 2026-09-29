package com.ecommerece_rag_service.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.sqlite.SQLiteConfig;
import org.sqlite.JDBC;

@Configuration
@EnableConfigurationProperties(SqliteVecProperties.class)
public class SqliteVecConfiguration {

	public static final String QUALIFIER = "sqliteVec";

	@Bean(name = "sqliteVecDataSource")
	@Qualifier(QUALIFIER)
	DataSource sqliteVecDataSource(SqliteVecProperties properties) {
		ensureSqliteParentDirectory(properties.getDatabaseUrl());

		SQLiteConfig config = new SQLiteConfig();
		config.enableLoadExtension(true);
		config.enforceForeignKeys(true);
		config.setBusyTimeout(5_000);

		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		dataSource.setDriverClass(JDBC.class);
		dataSource.setUrl(properties.getDatabaseUrl());
		dataSource.setConnectionProperties(config.toProperties());

		return properties.isEnabled()
				? new SqliteVecDataSource(dataSource, properties.getExtensionPath())
				: dataSource;
	}

	private static void ensureSqliteParentDirectory(String databaseUrl) {
		String prefix = "jdbc:sqlite:";
		if (databaseUrl == null || !databaseUrl.startsWith(prefix)) {
			return;
		}

		String pathPart = databaseUrl.substring(prefix.length());
		int queryIndex = pathPart.indexOf('?');
		if (queryIndex >= 0) {
			pathPart = pathPart.substring(0, queryIndex);
		}
		if (pathPart.isBlank() || ":memory:".equals(pathPart) || pathPart.startsWith("file::memory:")) {
			return;
		}
		if (pathPart.startsWith("file:")) {
			pathPart = pathPart.substring("file:".length());
		}

		Path databasePath = Path.of(pathPart).toAbsolutePath().normalize();
		Path parent = databasePath.getParent();
		if (parent == null) {
			return;
		}
		try {
			Files.createDirectories(parent);
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to create SQLite parent directory: " + parent, exception);
		}
	}

	@Bean(name = "sqliteVecJdbcTemplate")
	@Qualifier(QUALIFIER)
	JdbcTemplate sqliteVecJdbcTemplate(
			@Qualifier(QUALIFIER) DataSource sqliteVecDataSource) {
		return new JdbcTemplate(sqliteVecDataSource);
	}

	@Bean
	ApplicationRunner initializeSqliteVecSchema(
			@Qualifier(QUALIFIER) JdbcTemplate sqliteVecJdbcTemplate, SqliteVecProperties properties) {
		return arguments -> {
			if (!properties.isEnabled()) {
				return;
			}
			sqliteVecJdbcTemplate.execute("""
					CREATE VIRTUAL TABLE IF NOT EXISTS rag_documents USING vec0(
					    embedding float[%d],
					    +document_id TEXT,
					    +entity_type TEXT,
					    +sku TEXT,
					    +category_id TEXT,
					    +category_path TEXT,
					    +status TEXT,
					    +currency TEXT,
					    +price TEXT,
					    +content TEXT
					)
					""".formatted(properties.getDimensions()));

			sqliteVecJdbcTemplate.execute("""
					CREATE TABLE IF NOT EXISTS rag_index_status (
					    entity_type TEXT NOT NULL,
					    entity_id TEXT NOT NULL,
					    content_hash TEXT NOT NULL,
					    indexed_at TEXT NOT NULL,
					    PRIMARY KEY (entity_type, entity_id)
					)
					""");
		};
	}
}
