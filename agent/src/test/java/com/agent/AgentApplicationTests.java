package com.agent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"amazon.sp-api.endpoint=https://sellingpartnerapi-eu.amazon.com",
		"amazon.sp-api.region=eu-west-1",
		"amazon.sp-api.marketplace-id=A21TJRUUN4KGV",
		"amazon.sp-api.lwa-client-id=test-client-id",
		"amazon.sp-api.lwa-client-secret=test-client-secret",
		"amazon.sp-api.lwa-refresh-token=test-refresh-token",
		"spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.orm.jpa.hibernate.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.ai.vectorstore.chroma.autoconfigure.ChromaVectorStoreAutoConfiguration,org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration"
})
class AgentApplicationTests {

	@Test
	void contextLoads() {
	}

}
