# Microservices Parent

A multi-module Maven project for an ecommerce platform built from independent Spring Boot
microservices plus an AI chat agent. The parent POM aggregates the modules below so they can be
built together, while each service remains independently runnable and deployable.

## Modules

| Module | Artifact dir | Purpose |
| --- | --- | --- |
| [Agent](agent/README.md) | [`agent/`](agent) | Spring Boot REST API for ecommerce data (orders, customers, payments, inventory) and Amazon Selling Partner API (SP-API) order operations. Backed by MySQL. |
| [Ecommerce Auth Server](ecommerce-auth-server/README.md) | [`ecommerce-auth-server/`](ecommerce-auth-server) | Spring Authorization Server configured for the OAuth2 `client_credentials` grant only. Issues signed JWTs for machine-to-machine calls between the other services. |
| [Springboot MCP Server](springboot-mcp-server/README.md) | [`springboot-mcp-server/`](springboot-mcp-server) | Exposes the Agent's ecommerce API as [MCP](https://modelcontextprotocol.io/) tools (e.g. `getAllOrders`, `createProduct`) that MCP-compatible AI clients can discover and call over HTTP. |
| [Ecommerce RAG Service](ecommerece-rag-service/README.md) | [`ecommerece-rag-service/`](ecommerece-rag-service) | Indexes the product/category catalog from MySQL into vector embeddings (via Ollama) stored in SQLite (`sqlite-vec`), and serves semantic search over the catalog. |
| [Ecommerce AI Agent](ecommerce-ai-agent/README.md) | [`ecommerce-ai-agent/`](ecommerce-ai-agent) | Web chat assistant (Spring AI + Ollama) for ecommerce questions and operations. Calls the MCP server for live tools and the RAG service for catalog search. |

## Architecture

```text
                         ┌────────────────────────┐
                         │  ecommerce-auth-server  │
                         │  (OAuth2 client_creds)  │
                         └───────────▲─────────────┘
                                      │ issues/validates JWTs
            ┌─────────────────────────┼─────────────────────────┐
            │                         │                         │
┌───────────┴───────────┐  ┌──────────┴──────────┐   ┌───────────┴──────────┐
│  ecommerce-ai-agent    │  │ springboot-mcp-server│   │        agent         │
│  (chat UI / Spring AI) │──▶  (MCP tool server)   │──▶ (ecommerce REST API) │
└───────────┬────────────┘  └──────────────────────┘   └───────────┬──────────┘
            │                                                       │
            ▼                                                       ▼
   ecommerece-rag-service                                        MySQL
   (catalog semantic search,                              (orders, customers,
    Ollama embeddings + SQLite)                             products, payments)
```

The auth server issues and validates all tokens; each other service authenticates independently
with its own registered client credentials, so no caller's token is forwarded between hops.

## Requirements

- Java 17 or later
- Maven (or the Maven wrapper included in each module)
- MySQL (for the `agent`, `ecommerce-auth-server`, and `ecommerece-rag-service` modules)
- [Ollama](https://ollama.com/) (for the RAG service and AI agent)
- The `sqlite-vec` loadable extension (for the RAG service)

See each module's README for exact versions, environment variables, and setup steps.

## Building

Build all modules from the repository root:

```powershell
.\mvnw.cmd clean install
```

Or, on macOS/Linux:

```sh
./mvnw clean install
```

To build or run a single module, use its own Maven wrapper from within that module's directory
(see the module's README for details), for example:

```powershell
cd agent
.\mvnw.cmd spring-boot:run
```

## Suggested startup order

Because the services depend on each other for authentication and data, start them in roughly this
order for a full local environment:

1. MySQL (with the required schemas/tables for `agent`, `ecommerce-auth-server`, and
   `ecommerece-rag-service`)
2. Ollama (with the embedding and chat models pulled)
3. `ecommerce-auth-server` — issues/validates all OAuth2 tokens
4. `agent` — the ecommerce REST API
5. `springboot-mcp-server` — exposes the agent's API as MCP tools
6. `ecommerece-rag-service` — catalog semantic search
7. `ecommerce-ai-agent` — the chat UI that ties everything together

## Testing

Run each module's tests from its own directory with its Maven wrapper, for example:

```powershell
cd agent
.\mvnw.cmd test
```

## License

No license has been specified for this project.
