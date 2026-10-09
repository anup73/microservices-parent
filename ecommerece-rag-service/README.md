# E-commerce RAG Service

A Spring Boot service that indexes an e-commerce catalog for semantic search. It reads product
and category data from MySQL, creates embeddings with Ollama, and stores searchable vectors in
SQLite using the `sqlite-vec` extension.

Search returns relevant catalog documents; it does not generate an LLM answer. MySQL remains the
source of truth for frequently changing values such as price and inventory.

## How it works

1. `POST /api/rag/reindex` reads products and categories from MySQL.
2. The service builds one document per product or category, including the category breadcrumb.
3. Documents whose content has not changed are skipped; changed documents are embedded in batches
   and upserted into SQLite. Documents no longer present in MySQL are removed.
4. `GET /api/rag/search` embeds the query and returns the nearest documents from SQLite.

## Requirements

- Java 17 or later
- MySQL accessible to the service, with an `ecommerce_db` database and the `category` and
  `product` tables described below
- [Ollama](https://ollama.com/) running locally or at a configured URL, with an embedding model
  that supports `POST /api/embed`
- The platform-specific loadable `sqlite-vec` extension (`vec0.dll`, `vec0.so`, or `vec0.dylib`)

The default configuration uses port `8081`, MySQL at `localhost:3307`, Ollama at
`http://localhost:11434`, and a SQLite database at `./data/ecommerce-rag.db`.

## Catalog database

The service expects these tables to exist in MySQL. It does not create or migrate the catalog
schema.

| Table | Required columns |
| --- | --- |
| `category` | `category_id`, `name`, `description`, `parent_category_id`, `status` |
| `product` | `product_id`, `sku`, `name`, `description`, `category_id`, `price`, `currency`, `status` |

`parent_category_id` and `category_id` may be nullable where the catalog permits uncategorized
entries. The configured database user needs permission to read these tables.

## Configure and run

### 1. Set up Ollama

Install and start Ollama, then pull the default embedding model:

```sh
ollama pull nomic-embed-text
```

The embedding model's vector dimension must match `app.sqlite-vec.dimensions`. The default is
`768`, which matches `nomic-embed-text`. Choose a local embedding model that supports `/api/embed`;
chat-only models are not suitable for this endpoint.

### 2. Install sqlite-vec

Download the loadable archive for your operating system from the
[sqlite-vec releases](https://github.com/asg017/sqlite-vec/releases) and extract its `vec0`
library. Configure its absolute path using `app.sqlite-vec.extension-path` in
`src/main/resources/application.properties` or a Spring Boot configuration override. For example:

```properties
# Windows
app.sqlite-vec.extension-path=C:/tools/sqlite-vec/vec0.dll

# Linux
# app.sqlite-vec.extension-path=/opt/sqlite-vec/vec0.so

# macOS
# app.sqlite-vec.extension-path=/opt/sqlite-vec/vec0.dylib
```

The extension must match the deployment platform. SQLite creates the parent directory for the
configured database file if needed. If you change the embedding dimension after creating the
vector database, point `app.sqlite-vec.database-url` at a new SQLite file and reindex; the
existing vector table keeps its original dimension.

### 3. Configure MySQL

Set the `spring.datasource.*` properties for your MySQL instance. The defaults in
`src/main/resources/application.properties` target `localhost:3307/ecommerce_db`; override the
URL, username, and password for your environment. Do not commit real credentials.

### 4. Build and start

On Windows PowerShell:

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```sh
./mvnw clean package
./mvnw spring-boot:run
```

Alternatively, run the packaged jar:

```sh
java -jar target/ecommerece-rag-service-0.0.1-SNAPSHOT.jar
```

The service listens on `http://localhost:8081` by default. Configure the SQLite extension,
database connection, and any other environment-specific settings before starting.

## API

### Reindex the catalog

Run this after starting the service and whenever you want it to synchronize the vector index with
the current MySQL catalog:

```sh
curl -X POST http://localhost:8081/api/rag/reindex
```

The response reports total, indexed, and unchanged category/product counts and the number of stale
documents removed. For example:

```json
{
  "categoriesTotal": 12,
  "categoriesIndexed": 12,
  "categoriesSkippedUnchanged": 0,
  "productsTotal": 150,
  "productsIndexed": 150,
  "productsSkippedUnchanged": 0,
  "staleDocumentsRemoved": 0
}
```

### Search the index

`q` is required. `limit` is optional and defaults to `10`.

```sh
curl "http://localhost:8081/api/rag/search?q=wireless%20headphones&limit=10"
```

The response is a JSON array of matches. Each result includes the document ID, entity type, SKU,
category details, status, price snapshot, document content, and vector distance. Category
documents may have `null` SKU, currency, and price.

```json
[
  {
    "documentId": "product-100",
    "entityType": "PRODUCT",
    "sku": "WH-100",
    "categoryId": "12",
    "categoryPath": "Electronics > Audio > Headphones",
    "status": "ACTIVE",
    "currency": "INR",
    "price": "4999.00",
    "content": "Product: Wireless Headphones\n...",
    "distance": 0.12
  }
]
```

The price is the value captured during indexing, not a live price. Use the returned SKU or
document ID to query MySQL for current price, stock, or other frequently changing data.

## Monitoring

Spring Boot Actuator exposes:

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/prometheus`

Prometheus metrics are enabled through the Micrometer Prometheus registry. OTLP metrics and trace
export are disabled by default; configure an appropriate collector before enabling them.

## Tests

Run the test suite with the Maven wrapper:

```sh
./mvnw test
```

On Windows PowerShell, use `.\mvnw.cmd test`. The SQLite vector repository integration test is
enabled only when the `app.sqlite-vec.extension-path` system property is set to a valid extension
path.

## Configuration reference

| Property | Default | Purpose |
| --- | --- | --- |
| `server.port` | `8081` | HTTP port |
| `spring.datasource.url` | `jdbc:mysql://localhost:3307/ecommerce_db?...` | Catalog MySQL connection |
| `spring.datasource.username` | `admin` | Catalog MySQL user |
| `spring.datasource.password` | Not documented here | Catalog MySQL password; provide securely |
| `app.sqlite-vec.database-url` | `jdbc:sqlite:./data/ecommerce-rag.db` | Vector database file |
| `app.sqlite-vec.enabled` | `true` | Enable SQLite vector storage |
| `app.sqlite-vec.extension-path` | Configured in `application.properties` | Absolute path to the platform's `sqlite-vec` library |
| `app.sqlite-vec.dimensions` | `768` | Embedding dimension; must match the embedding model |
| `app.rag.ollama.base-url` | `http://localhost:11434` | Ollama server URL |
| `app.rag.ollama.model` | `nomic-embed-text` | Embedding model name |
| `app.rag.ollama.batch-size` | `32` | Number of documents sent per embedding batch |
| `app.rag.ollama.api-key` | None | Optional Ollama Cloud API key |

An Ollama Cloud API key can also be supplied with `APP_RAG_OLLAMA_API_KEY` or `OLLAMA_API_KEY`.
Do not commit API keys or database passwords to source control.
