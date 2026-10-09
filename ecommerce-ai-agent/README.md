# Ecommerce AI Agent

Spring Boot application that provides a web chat assistant for ecommerce questions and operations. It uses Spring AI with an Ollama chat model, connects to an MCP server for live tools, and exposes a RAG search function for product information and store policies.

## Features

- Browser-based chat UI served by the application.
- Chat API with conversation history held in memory for each HTTP session.
- Ollama-powered responses with MCP tools and a RAG search function.
- OAuth 2.0 client-credentials token retrieval for requests to the MCP server.
- Actuator health and Prometheus metrics endpoints.
- Trace logging to the console and `agent_trace.log`, plus optional OpenTelemetry exports.

## Technology

- Java 17
- Spring Boot 4.1.1
- Spring AI 2.0.1
- Spring MVC, Spring Security OAuth2 Client, and Spring Actuator
- Ollama, MCP Streamable HTTP client, and Maven

## Prerequisites

- JDK 17 or later
- Ollama running locally with the configured chat model available
- The ecommerce MCP server, authorization server, and RAG service reachable from this application
- Maven is optional; the project includes Maven wrapper scripts

The default service addresses are:

| Service | Default URL |
|---|---|
| Ollama | `http://localhost:11434` |
| RAG service | `http://localhost:8081` |
| MCP server | `http://localhost:8090/mcp` |
| OAuth2 authorization server token endpoint | `http://localhost:9000/oauth2/token` |
| This application | `http://localhost:8085` |

Make sure the required services are running and that the configured model, MCP connection, and OAuth2 client credentials match your environment.

## Run locally

From the project root, start the application with the Maven wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

Open [http://localhost:8085](http://localhost:8085) to use the chat interface.

To build and run the packaged application:

```powershell
.\mvnw.cmd clean package
java -jar target\ecommerce-ai-agent-0.0.1-SNAPSHOT.jar
```

## API

Send a plain-text question to the chat endpoint:

```powershell
curl.exe -X POST http://localhost:8085/api/chat/message `
  -H "Content-Type: text/plain" `
  -d "What is your return policy?"
```

The response is plain text. The application uses the HTTP session to identify a conversation, so clients should retain and reuse the session cookie to continue the same conversation.

## Configuration

Application defaults are in [`application.properties`](src/main/resources/application.properties). Adjust them for the services and credentials in your environment.

| Setting | Default | Purpose |
|---|---|---|
| `server.port` | `8085` | HTTP port for the agent |
| `spring.ai.ollama.base-url` | `http://localhost:11434` | Ollama service URL |
| `spring.ai.ollama.chat.options.model` | `gemma4:31b-cloud` | Ollama chat model |
| `rag.server.url` | `http://localhost:8081` | RAG service base URL |
| `spring.ai.mcp.client.streamable-http.connections.ecommerce.url` | `http://localhost:8090` | MCP server base URL |
| `spring.ai.mcp.client.streamable-http.connections.ecommerce.endpoint` | `/mcp` | MCP endpoint path |
| `spring.security.oauth2.client.provider.mcp-server.token-uri` | `http://localhost:9000/oauth2/token` | Authorization server token endpoint |
| `chat.memory.max-messages` | `50` | Maximum messages retained in the in-memory chat window |

The OAuth2 client registration is named `mcp-server` and uses the `client_credentials` grant. Its client ID and secret are configured in `application.properties`; use credentials registered with the authorization server. Do not use development credentials in shared or production environments.

OpenTelemetry trace and metric export are disabled by default. The properties file documents the `OTLP_METRICS_ENABLED`, `TRACE_EXPORT_ENABLED`, `OTEL_TRACES_ENDPOINT`, and `LOKI_OTLP_ENDPOINT` settings for enabling and configuring those exports.

## Monitoring

The application exposes:

- `GET /actuator/health`
- `GET /actuator/prometheus`

The agent also writes interaction trace events to `agent_trace.log` in its working directory. Trace entries can include user prompts and model/tool responses; protect the log and review data-retention requirements before using it with sensitive information.

## Project structure

```text
src/main/java/com/ecommerce_ai_agent/
├── EcommerceAiAgentApplication.java
├── ServletInitializer.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   └── ChatController.java
├── service/
│   ├── AiAgentService.java
│   └── RagClientService.java
└── util/
    └── AgentTracer.java

src/main/resources/
├── application.properties
├── logback-spring.xml
└── static/
    └── index.html
```

## Tests

Run the test suite with:

```powershell
.\mvnw.cmd test
```

## License

No license has been specified for this project.
