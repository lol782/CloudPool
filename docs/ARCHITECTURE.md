# CloudPool Architecture

## 1. System Topology Overview

CloudPool is a Backend-as-a-Service (BaaS) and developer orchestration platform built as a multi-protocol gateway over core development databases, cache systems, and cloud storage providers.

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐  │
│  │  Web Browser │  │  Mobile App  │  │  CLI / SDK      │  │
│  └──────────────┘  └──────────────┘  └─────────────────┘  │
└────────────┬──────────────────────────────────┬─────────────┘
             │                                  │
             ▼                                  ▼
┌─────────────────────────────────────────────────────────────┐
│                  API Gateway Layer (Nginx)                  │
└────────────┬──────────────────────────────────┬─────────────┘
             │                                  │
             ▼                                  ▼
┌─────────────────────────────────────────────────────────────┐
│                Application Layer (Spring Boot)              │
│  ┌──────────────────┐  ┌──────────────────┐  ┌────────────┐ │
│  │ GraphQL Resolvers│  │ REST Controllers │  │  Security  │ │
│  └────────┬─────────┘  └────────┬─────────┘  └──────┬─────┘ │
│           │                     │                   │       │
│           ▼                     ▼                   ▼       │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                     Service Layer                      │ │
│  │  - StorageService    - DatabaseService                 │ │
│  │  - VectorService     - BackgroundJobService            │ │
│  └──────────────────────────┬─────────────────────────────┘ │
│                             │                               │
│                             ▼                               │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                  Rust FFI Integration                  │ │
│  │  (sha256 checksums, gzip compress, vector math FFI)    │ │
│  └────────────────────────────────────────────────────────┘ │
└──────┬────────────────────┬────────────────────┬───────────┘
       │                    │                    │
   ┌───▼────┐          ┌────▼────┐          ┌────▼───────┐
   │PostgreSQL          │Redis    │          │RabbitMQ    │
   │Database            │Cache    │          │Queue       │
   └────────┘          └─────────┘          └────────────┘
       │
   ┌───▼──────────────┐
   │Weaviate          │
   │Vector DB         │
   └──────────────────┘
       │
   ┌───▼──────────────┐
   │Google Drive API  │
   │Storage Pool      │
   └──────────────────┘
```

---

## 2. Platform Core Services

### 🔒 Security & Multi-Tenancy (Tenant Scopes)
All active requests undergo JWT decryption or API key authentication filter steps. Once authentication is resolved:
1. `TenantFilter` interceptor reads user identification scopes.
2. The user ID is stored thread-safely in `TenantContextHolder` (ThreadLocal context).
3. Database repositories extending `TenantAwareRepository` execute database queries scoped using custom SpEL filters:
   `userId = TenantContextHolder.getTenantId()`

### ⚡ Performance Booster (Rust FFI Bridge)
CPU-bound operations are processed by native compiled Rust logic. The JVM loads the native shared library (`cloudpool_rust.dll`/`libcloudpool_rust.so`) via JNI mappings:
* **Checksumming**: SHA-256 algorithm calculating hash strings.
* **Compression**: Speed-optimized Gzip dynamic compress/decompress wrappers.
* **Vector Math**: Fast Euclidean and cosine similarity calculations.

### 🧠 Semantic Vector Search
User documents are split, enqueued, and embedded into 1536-dimensional vectors using OpenAI's Ada models.
* Dynamic query matching is offloaded to a Weaviate vector cluster.
* In environments without credentials, a local H2-backed vector database performs high-performance cosine similarity math calculations.

### 📭 Async Event Processing
A RabbitMQ message broker receives asynchronous events:
* **File Uploads**: Checksum hashing and size audit logs are enqueued.
* **OpenAI Embedding Queries**: Text sequences are queued for bulk vector processing.
