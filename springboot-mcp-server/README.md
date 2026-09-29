# Ecommerce MCP Server

A Spring Boot [MCP](https://modelcontextprotocol.io/) server that exposes the
`ecommerce-api-server` (the `agent` project) as a set of **MCP tools** — e.g.
`getAllOrders`, `getCustomerDetails`, `createProduct` — that any MCP-compatible
AI client can discover and call over HTTP (Streamable HTTP / SSE).

It sits between:

- **MCP clients** (AI assistants, IDE agents, etc.) that call tools over `/mcp`
- **ecommerce-auth-server** — the OAuth2 authorization server (issues/validates JWTs)
- **ecommerce-api-server** (`agent`) — the real REST API + database backing the store

```
MCP Client  --(Bearer JWT)-->  MCP Server  --(own Bearer JWT)-->  Ecommerce API Server
                                    |                                    |
                                    +------------> ecommerce-auth-server <+
                                         (issues & verifies all tokens)
```

## Why two separate tokens?

The MCP server does **not** forward the caller's token to the ecommerce API.
Instead:

1. The caller's JWT is validated by the MCP server acting as an **OAuth2 resource server**.
2. The MCP server then mints **its own** JWT (via `client_credentials`) to call the
   ecommerce API, acting as an **OAuth2 client** with its own registered
   identity (`mcp-server-client`).

This means each hop is independently authenticated and each service's
credentials can be rotated or revoked without affecting the other. See the
full request lifecycle in [`mcp-auth-sequence.png`](./mcp-auth-sequence.png)
(source: [`mcp-auth-sequence.mmd`](./mcp-auth-sequence.mmd)).

## How it works, in plain terms

**At startup** (happens once):
1. The MCP server asks the auth-server "how do I check tokens?" — it discovers
   the JWKS (public signing keys) endpoint and token endpoint.
2. It configures its own gatekeeper (`SecurityConfig`) using that info, so it
   can reject anyone without a valid pass.
3. It also prepares an "outgoing pass" mechanism (`OAuth2ClientConfig`) that
   will fetch its own ID token whenever it needs to call the ecommerce API later.

**On every tool call** (happens per request):
1. A client sends a request to the MCP server with a Bearer token, asking to
   run a tool (e.g. "get all orders").
2. The MCP server verifies that token is genuine and not expired (using the
   cached public key from step 1) — like a bouncer scanning an ID.
3. If valid, it runs the requested `@Tool` method.
4. That method needs data from the ecommerce API, so it uses a `RestClient` to
   call it.
5. Before making that call, the client checks: "Do I have a valid ID to show
   them?" If not (or expired), it asks the auth-server for a fresh one using
   `client_credentials` (proving the *service* is legitimate, not a specific
   user) and caches it for reuse.
6. The HTTP call to the ecommerce API includes that token as
   `Authorization: Bearer ...`.
7. The ecommerce API performs the same badge check and, if satisfied, returns
   the real data (JSON), which flows back up to the original caller.

## Component roles

### `springboot-mcp-server` (this project)

| Class | Role |
|---|---|
| `SpringbootMcpServerApplication` | Application entry point. |
| `SecurityConfig` | Configures this app as an OAuth2 **resource server** — every MCP request must carry a valid JWT (except `/actuator/health`). |
| `OAuth2ClientConfig` | Configures this app as an OAuth2 **client**. Builds the `OAuth2AuthorizedClientManager` (fetches/caches `client_credentials` tokens) and the `RestClient` bean (`ecommerceApiRestClient`) that auto-attaches a fresh Bearer token to every outbound call via `OAuth2ClientHttpRequestInterceptor`. |
| `McpToolConfig` | Registers the `@Tool` methods in `EcommerceTools` as MCP tools via `MethodToolCallbackProvider`. |
| `EcommerceTools` | The menu of tools an MCP client can call (`getAllOrders`, `getCustomerDetails`, `createProduct`, `updateProductInventory`, etc.). Delegates to `EcommerceApiClient`. |
| `EcommerceApiClient` | Thin wrapper that makes the actual HTTP calls to the ecommerce API server's `/api/ecommerce/**` endpoints. |
| `dto/*` | Plain data classes mirroring the ecommerce API's request/response shapes. |

### `ecommerce-auth-server`

| Class | Role |
|---|---|
| `AuthorizationServerConfig` | Defines the OAuth2 authorization server: <br>• Security filter chain protecting `/oauth2/token`, `/oauth2/jwks`, etc. <br>• `registeredClientRepository` — the list of known clients allowed to request tokens (`agent-client`, `mcp-server-client`), each with its own secret and scopes. <br>• `jwkSource` — generates the RSA key pair used to sign tokens (public key exposed via JWKS for verification). <br>• `authorizationServerSettings` — sets the issuer URL embedded in every token. |

### `agent` (ecommerce-api-server)

| Class | Role |
|---|---|
| `SecurityConfig` | Makes this app an OAuth2 resource server too — every request must carry a valid JWT signed by the same auth-server. |
| `EcommerceController` | The REST API (`/api/ecommerce/orders`, `/customers`, `/payments`, `/products`, ...) called by the MCP server. |
| `EcommerceQueryService` | Business logic layer, backed by JPA repositories and MySQL. |

## Running

### Prerequisites
- Java 17
- MySQL running and reachable by `ecommerce-api-server` (see its `application-ecommerce.properties`)
- `ecommerce-auth-server` and `agent` (ecommerce-api-server) projects available as siblings

### Start order
1. **ecommerce-auth-server** (port `9000` by default)
   ```
   cd ecommerce-auth-server
   .\mvnw.cmd spring-boot:run
   ```
2. **agent / ecommerce-api-server** (port `8080` by default)
   ```
   cd agent
   .\mvnw.cmd spring-boot:run
   ```
3. **springboot-mcp-server** (port `8090` by default)
   ```
   cd springboot-mcp-server
   .\mvnw.cmd spring-boot:run
   ```
   or, after building the WAR:
   ```
   .\mvnw.cmd clean package
   java -jar target\springboot-mcp-server-0.0.1-SNAPSHOT.war
   ```

### Configuration

Key properties in `src/main/resources/application.properties`:

```properties
server.port=${SERVER_PORT:8090}

# MCP transport
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.protocol=STREAMABLE

# Inbound auth (this server validates caller tokens)
spring.security.oauth2.resourceserver.jwt.issuer-uri=${OAUTH_ISSUER_URI:http://localhost:9000}

# Outbound auth (this server authenticates itself to the ecommerce API)
spring.security.oauth2.client.registration.agent-api.client-id=${OAUTH_MCP_CLIENT_ID:mcp-server-client}
spring.security.oauth2.client.registration.agent-api.client-secret=${OAUTH_MCP_CLIENT_SECRET:mcp-server-secret}
spring.security.oauth2.client.registration.agent-api.authorization-grant-type=client_credentials
spring.security.oauth2.client.registration.agent-api.scope=agent.read,agent.write
spring.security.oauth2.client.provider.agent-api.token-uri=${OAUTH_TOKEN_URI:http://localhost:9000/oauth2/token}

# Ecommerce API base URL
ecommerce.api.base-url=${ECOMMERCE_API_BASE_URL:http://localhost:8080}
```

> **Note:** `spring.ai.mcp.server.protocol=STREAMABLE` must be set explicitly —
> the Spring AI auto-configuration's `@ConditionalOnProperty` check does not
> fall back to the Java-side default enum value.

### Testing the server manually

1. Get a token from the auth server:
   ```
   curl -u agent-client:agent-secret -X POST http://localhost:9000/oauth2/token ^
        -d "grant_type=client_credentials&scope=agent.read agent.write"
   ```
2. Initialize an MCP session:
   ```
   curl -X POST http://localhost:8090/mcp ^
        -H "Authorization: Bearer <token>" ^
        -H "Content-Type: application/json" ^
        -H "Accept: application/json, text/event-stream" ^
        -d "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test\",\"version\":\"1.0\"}}}"
   ```
   The response includes an `Mcp-Session-Id` header — reuse it on subsequent calls.
3. List tools (`tools/list`) or call one (`tools/call` with `name`/`arguments`)
   using the same session id.

## Available tools

| Tool | Description |
|---|---|
| `getAllOrders` | List all orders |
| `getOrderByReference` | Get a single order by reference/number |
| `getOrdersByProductId` | List orders containing a given product |
| `getOrdersByCustomerId` | List orders placed by a given customer |
| `getAllCustomers` | List all customers |
| `getCustomerDetails` | Get a customer's details and addresses |
| `getAllPayments` | List all payments |
| `getPaymentsByOrderReference` | List payments for an order |
| `getPaymentsByProductId` | List payments for orders containing a product |
| `createProduct` | Create a new product |
| `updateProductInventory` | Update a product's available/reserved quantities |
| `updateProductImages` | Replace a product's images |
