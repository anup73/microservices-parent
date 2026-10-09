# ecommerce-auth-server

A minimal [Spring Authorization Server](https://spring.io/projects/spring-authorization-server) configured **exclusively for the OAuth2 `client_credentials` grant**, providing machine-to-machine (M2M) access tokens for other services in the ecommerce platform (e.g. an "agent" resource server and an MCP server). There are no user login, consent, or OIDC endpoints — this server only issues signed JWT access tokens to pre-registered confidential clients.

## Features

- OAuth2 `client_credentials` grant only (`/oauth2/token`)
- JWT access tokens signed with an in-memory RSA key pair, exposed via `/oauth2/jwks`
- Three pre-registered machine clients with distinct scopes (see [Registered clients](#registered-clients))
- Request logging for `/oauth2/token` calls (client id + grant type only — secrets are never logged)
- Token-issuance logging (client id, grant type, scopes, TTL) via a custom `OAuth2TokenCustomizer`
- Observability out of the box: Micrometer + Prometheus metrics, OpenTelemetry tracing/log export (e.g. to Loki/Tempo), and trace/span IDs correlated into console logs
- Spring Boot Actuator endpoints (`health`, `info`, `prometheus`)

## Tech stack

- Java 17
- Spring Boot 4.1.1 (Spring Boot starter parent)
- Spring Security + Spring Authorization Server
- Spring Web MVC on embedded Tomcat
- MySQL (JDBC datasource; no JPA entities are currently persisted — `ddl-auto=none`)
- Micrometer / Prometheus, OpenTelemetry (logging + tracing bridge)
- Maven (via the included `mvnw`/`mvnw.cmd` wrapper)

## Prerequisites

- JDK 17+
- Maven (or use the bundled wrapper — no local Maven install required)
- A reachable MySQL instance (see [Configuration](#configuration)) if you keep the datasource enabled
- Optional: a Loki/Tempo (or other OTLP-compatible) collector if you want to enable trace/log export

## Getting started

Clone the repository, then from the project root:

```powershell
# Run the app (uses the Maven wrapper, no local Maven needed)
.\mvnw.cmd spring-boot:run

# Or build a runnable jar
.\mvnw.cmd clean package
java -jar target\ecommerce-auth-server-0.0.1-SNAPSHOT.jar
```

By default the server listens on port `9000` (configurable via `SERVER_PORT`).

## Configuration

All configuration lives in [application.properties](/c:/RebindRise/spring_workspace/ecommerce-auth-server/src/main/resources/application.properties) and is overridable via environment variables.

### Datasource

| Property | Env var | Default |
|---|---|---|
| `spring.datasource.url` | — | `jdbc:mysql://localhost:3307/ecommerce_db?...` |
| `spring.datasource.username` | — | `admin` |
| `spring.datasource.password` | — | *(set in your own `application.properties` / env; not committed)* |

### OAuth2 issuer & clients

| Property | Env var | Default | Purpose |
|---|---|---|---|
| `oauth.issuer-uri` | `OAUTH_ISSUER_URI` | `http://localhost:9000` | Public URL embedded as the `iss` claim; must match resource servers' `issuer-uri` |
| `oauth.client.id` / `oauth.client.secret` | `OAUTH_CLIENT_ID` / `OAUTH_CLIENT_SECRET` | `agent-client` / `{noop}agent-secret` | Primary M2M client (e.g. Postman or another backend) |
| `oauth.mcp-client.id` / `oauth.mcp-client.secret` | `OAUTH_MCP_CLIENT_ID` / `OAUTH_MCP_CLIENT_SECRET` | `mcp-server-client` / `{noop}mcp-server-secret` | Used by the springboot-mcp-server to call the agent API |
| `oauth.ai-agent.id` / `oauth.ai-agent.secret` | `OAUTH_AI_AGENT_ID` / `OAUTH_AI_AGENT_SECRET` | `mcp-agent-client` / `{noop}mcp-agent-secret` | Used by the AI agent to call the MCP server |

> ⚠️ The default secrets use the `{noop}` (plaintext) password encoder and are suitable for local development only. Override them via environment variables for any shared or production environment.

### Observability

| Property | Env var | Default | Purpose |
|---|---|---|---|
| `management.otlp.metrics.export.enabled` | `OTLP_METRICS_ENABLED` | `false` | Enable OTLP metrics export once a collector exists |
| `management.tracing.export.enabled` | `TRACE_EXPORT_ENABLED` | `false` | Enable trace export once a collector (Tempo/Jaeger) exists |
| `management.opentelemetry.tracing.export.otlp.endpoint` | `OTEL_TRACES_ENDPOINT` | `http://localhost:4318/v1/traces` | OTLP traces endpoint |
| `management.opentelemetry.logging.export.otlp.endpoint` | `LOKI_OTLP_ENDPOINT` | `http://localhost:3100/otlp/v1/logs` | Loki's native OTLP log ingestion endpoint |

Actuator endpoints are exposed at `/actuator/health`, `/actuator/info`, and `/actuator/prometheus` (unauthenticated).

## Registered clients

All three clients use `client_secret_basic` authentication and the `client_credentials` grant, with a 30-minute access token TTL.

| Client (default id) | Scopes |
|---|---|
| `agent-client` | `agent.read`, `agent.write` |
| `mcp-server-client` | `mcp.read`, `mcp.write`, `agent.read`, `agent.write` |
| `mcp-agent-client` | `agent.read`, `agent.write`, `mcp.read`, `mcp.write` |

## Requesting a token

```powershell
curl -X POST http://localhost:9000/oauth2/token `
  -u agent-client:agent-secret `
  -d "grant_type=client_credentials" `
  -d "scope=agent.read agent.write"
```

The response is a standard OAuth2 token response containing a signed JWT `access_token`. The server's public signing key is available at `http://localhost:9000/oauth2/jwks` for resource servers to validate tokens.

## Project structure

```
src/main/java/com/ecommerce_auth_server/
├── EcommerceAuthServerApplication.java   # Spring Boot entry point
├── ServletInitializer.java               # WAR deployment support
└── config/
    ├── AuthorizationServerConfig.java        # OAuth2 authorization server, clients, JWK source
    └── OAuth2TokenRequestLoggingFilter.java   # Logs inbound /oauth2/token requests (no secrets)
src/main/resources/
├── application.properties                # Datasource, OAuth2, observability configuration
└── logback-spring.xml                    # Logging configuration (console + OTEL appender)
```

## Testing

```powershell
.\mvnw.cmd test
```

## License

No license has been specified for this project.
