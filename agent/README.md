# Agent

A Spring Boot REST service for ecommerce data and Amazon Selling Partner API (SP-API) order operations. It uses Java 17, Spring Data JPA, and MySQL for the ecommerce API.

## Requirements

- Java 17 or later
- MySQL 8.x for the ecommerce profile
- Maven (or the included Maven wrapper)
- An OAuth2/OIDC authorization server that issues JWTs for API access
- Amazon SP-API/LWA credentials when using the Amazon profile

## Getting started

### 1. Configure the ecommerce database

The default profile is `ecommerce`. Its datasource is configured in `src/main/resources/application-ecommerce.properties` and expects MySQL at `localhost:3307`, with a database named `ecommerce_db`. Update the JDBC URL and database credentials in that file to match your local MySQL instance.

To create a fresh development database, run `ecommercedb.sql` against MySQL. This script drops and recreates `ecommerce_db`; **do not run it against a database whose data you need to keep**. Optionally load the sample records from `dummydata.sql` after creating the schema.

### 2. Configure JWT authentication

All API routes require a valid bearer JWT, except `/actuator/health` and `/actuator/prometheus`. Configure either the issuer URL (recommended) or the JWK set URL for your authorization server:

```text
OAUTH_ISSUER_URI=https://your-auth-server.example.com
```

Or:

```text
OAUTH_JWK_SET_URI=https://your-auth-server.example.com/.well-known/jwks.json
```

The default issuer is `http://localhost:9000`. Point it at a running authorization server (or set the JWK set URL) before making authenticated API requests; requests must carry a valid token issued by that server.

### 3. Run the application

From the repository root:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The service listens on port `8080` by default. Build a runnable JAR with `./mvnw package` (Windows: `.\mvnw.cmd package`), then run it with `java -jar target/agent-0.0.1-SNAPSHOT.jar`.

To run the test suite:

```bash
./mvnw test
```

## Profiles

The active profile is set to `ecommerce` in `application.properties`.

| Profile | Purpose | Configuration |
| --- | --- | --- |
| `ecommerce` | Ecommerce REST API backed by MySQL | `application-ecommerce.properties` |
| `amazon` | Amazon LWA token and SP-API order endpoints | `application-amazon.properties` |

Start with the Amazon profile using `./mvnw spring-boot:run -Dspring-boot.run.profiles=amazon` (Windows: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=amazon"`). Set the following environment variables first:

| Variable | Description |
| --- | --- |
| `LWA_CLIENT_ID` | Login with Amazon application client ID |
| `LWA_CLIENT_SECRET_KEY` | Login with Amazon client secret |
| `LWA_CLIENT_REFRESH_TOKEN` | SP-API refresh token |
| `AWS_IAM_ACCESS_KEY` | Optional AWS access key for SP-API signing |
| `AWS_IAM_SECRET_KEY` | Optional AWS secret key for SP-API signing |

The SP-API endpoint, AWS region, and marketplace ID are defined in `application-amazon.properties` and can be adjusted there for the target marketplace.

## Ecommerce API

Base path: `/api/ecommerce`

Send `Authorization: Bearer <access-token>` with each request. The following endpoints are available:

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/orders` | List orders |
| `GET` | `/orders/{orderReference}` | Get an order by reference |
| `GET` | `/products/{productId}/orders` | List orders containing a product |
| `GET` | `/customers` | List customers |
| `GET` | `/customers/{customerId}` | Get customer details |
| `GET` | `/customers/{customerId}/orders` | List a customer's orders |
| `GET` | `/payments` | List payments |
| `GET` | `/orders/{orderReference}/payments` | List payments for an order |
| `GET` | `/products/{productId}/payments` | List payments associated with a product |
| `GET` | `/products/{productId}/inventory` | Get inventory for a product |
| `GET` | `/inventory?productName={name}` | Search inventory by product name |
| `POST` | `/products` | Create a product |
| `PUT` | `/products/{productId}/inventory` | Update product inventory |
| `PUT` | `/products/{productId}/images` | Update product images |

Example request:

```bash
curl http://localhost:8080/api/ecommerce/orders \
  -H "Authorization: Bearer <access-token>"
```

Create and update operations accept JSON request bodies validated by the API. See the request DTOs in `src/main/java/com/agent/dto` for the accepted fields and constraints. Not-found resources return `404`; request validation errors return `400`.

## Amazon API

Base path: `/api/amazon` (available with the `amazon` profile). All endpoints also require a valid bearer JWT.

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/token` | Get an Amazon LWA access token |
| `GET` | `/orders` | Search SP-API orders |
| `GET` | `/orders/{orderId}` | Get an SP-API order |

Order search accepts query parameters including `marketplaceIds`, `createdAfter` or `lastUpdatedAfter` (exactly one is required), optional `createdBefore` or `lastUpdatedBefore`, `fulfillmentStatuses`, `fulfilledBy`, `maxResultsPerPage` (1–100), `paginationToken`, and `includedData`. For example:

```text
GET /api/amazon/orders?marketplaceIds=A21TJRUUN4KGV&createdAfter=2026-01-01T00:00:00Z
```

Order details optionally accept `includedData` query parameters.

## Operational endpoints

- `GET /actuator/health` — health status; public
- `GET /actuator/prometheus` — Prometheus metrics; public
- `GET /actuator/info` — application information; requires a valid bearer JWT

## Postman

Import `postman_collection.json` into Postman to try the ecommerce endpoints. Its default `baseUrl` is `http://localhost:8080`; provide a valid bearer token in the collection or request authorization settings before sending requests.
