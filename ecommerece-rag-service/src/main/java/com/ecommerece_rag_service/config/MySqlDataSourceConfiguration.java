package com.ecommerece_rag_service.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Presence of the sqlite-vec {@link DataSource} bean prevents Spring Boot's
 * DataSourceAutoConfiguration from running (it backs off whenever any DataSource bean already
 * exists). This bean recreates the equivalent MySQL datasource explicitly from the
 * {@code spring.datasource.*} properties and marks it {@code @Primary} so it remains the source
 * of truth used by the default {@link org.springframework.jdbc.core.JdbcTemplate}.
 *
 * <p>JdbcTemplateAutoConfiguration also backs off once any {@link JdbcTemplate} bean exists
 * (including the sqlite-vec one), so a primary MySQL {@link JdbcTemplate} is declared here.
 */
@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class MySqlDataSourceConfiguration {

	@Bean
	@Primary
	@ConfigurationProperties("spring.datasource.hikari")
	DataSource mysqlDataSource(DataSourceProperties properties) {
		return properties
				.initializeDataSourceBuilder()
				.type(com.zaxxer.hikari.HikariDataSource.class)
				.build();
	}

	@Bean
	@Primary
	JdbcTemplate jdbcTemplate(DataSource mysqlDataSource) {
		return new JdbcTemplate(mysqlDataSource);
	}
}
