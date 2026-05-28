# 🚀 CloudPool — Developer Infrastructure Orchestration & Decentralized BaaS Platform

> **A Developer Infrastructure Orchestration Platform with Versioned State, Provider Abstraction, Secure Local-to-Cloud Connectivity, and High-Performance Rust Extensions.**

CloudPool is a high-performance developer Backend-as-a-Service (BaaS) and orchestration platform. It pools storage (Google Drive + Local) and dynamic relational/vector database instances into a unified development console. It features native Rust performance optimization, vector search indexing, dynamic database provisioning, and a developer console dashboard.

Instead of replacing cloud providers, CloudPool acts as a unified control layer over your local and cloud developer infrastructure.

---

## 🧠 The Problem It Solves

Modern developers use fragmented tools and services during development:
- PostgreSQL (local / cloud)
- Redis instances
- File storage (Google Drive / S3)
- Local dev environments
- Multiple configuration systems
- No unified rollback or infrastructure state tracking

**CloudPool solves this by introducing a single orchestration layer for managing developer infrastructure state.**

---

## ⚙️ Core Features

* **🔐 Secure Authentication & User Scoping**: Spring Boot-based secure login system. User-scoped database and storage configuration profiles.
* **📦 Hybrid Storage Pooling**: Connect Google Drive storage or fallback to local disk storage, supporting file sharing with secure, expirable tokens and access audit logging.
* **🦀 Rust Integration Layer (FFI / JNI)**: Performance-sensitive operations like file checksumming (SHA-256), Gzip compression/decompression, and similarity vector mathematics are implemented in Rust and invoked via Java JNI.
* **⚡ Dynamic Table Provisioner & SQL Console**: Provision new database tables, define column schema structures, execute queries, and perform database operations dynamically on local H2 or remote PostgreSQL instances.
* **📸 Infrastructure Snapshots (Rollback Engine)**: Take versioned snapshots of database schemas, environment variables, and connections, and rollback infrastructure states in real-time.
* **🔍 Vector Search & Embeddings**: Semantic text chunk indexing and hybrid searches powered by OpenAI/local embeddings and Weaviate (with a local cosine-similarity vector search fallback).
* **🕸️ Multi-Protocol APIs**: Complete REST API and GraphQL schema with queries, mutations, and real-time console logs.
* **🌐 Secure Tunneling System (Agent-Based Design - *Planned*)**: Desktop agent to expose local services securely (ngrok-like tunneling mechanism) and connect local databases to CloudPool.
* **📊 Unified Developer Dashboard**: Manage infrastructure connections, execute queries (SQL + Redis CLI style), and monitor system state in a responsive console.

---

## 🧩 Tech Stack

| Component | Technology |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.x, Spring GraphQL, Spring Security, JPA / Hibernate |
| **Native Module** | Rust 1.70+ (using `jni`, `sha2`, `flate2`, `serde`) |
| **Databases** | H2 (Local In-Memory/File), PostgreSQL (Production), Weaviate (Vector DB), Redis (Cache) |
| **Frontend** | HTML5, Vanilla CSS, JetBrains Mono typography (Developer SPA console) |
| **Build Tools** | Maven 3.9.6 (local wrapper included), Cargo (Rust) |

---

## 📋 Prerequisites

Ensure you have the following installed:
* **JDK 17 or 21**
* **Rust & Cargo** (for compilation of native dynamic library)
* **Maven** (A local Maven 3.9.6 wrapper is included at `./apache-maven-3.9.6`)

---

## ⚙️ Compilation & Setup

### Step 1: Compile the Native Rust Library
The Spring Boot backend dynamically loads the compiled Rust binary to invoke FFI methods.
```bash
# Navigate to the rust directory
cd backend/rust

# Compile the release library
cargo build --release
```
*This produces `cloudpool_rust.dll` (Windows), `libcloudpool_rust.so` (Linux), or `libcloudpool_rust.dylib` (macOS) in `backend/rust/target/release/`.*

### Step 2: Run JNI Integration Test
Validate that the Java Virtual Machine can load and communicate with the compiled Rust binary successfully:
```bash
# Navigate to the spring-boot backend directory
cd ../spring-boot

# Execute the FFI integration runner
..\..\apache-maven-3.9.6\bin\mvn.cmd exec:java "-Dexec.mainClass=com.cloudpool.util.JniTest"
```

---

## 🏃 Running the Application

To run the Spring Boot application locally without Docker dependencies (using standalone H2 databases and local vector search):

```bash
# Navigate to the spring-boot backend directory
cd backend/spring-boot

# Run Spring Boot with local profile active
..\..\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

* The **Developer Console Dashboard** will be available at: [http://localhost:8080/index.html](http://localhost:8080/index.html)
* The **GraphQL API Endpoint** will be at: `http://localhost:8080/graphql`
* The **GraphiQL Interactive Playground** will be available at: [http://localhost:8080/graphiql](http://localhost:8080/graphiql)
* The **H2 Database Console** can be accessed at: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:file:./data/cloudpooldb`, Username: `sa`, Password: `password`)

---

## 📂 Project Structure

```
cloudpool/
├── backend/
│   ├── spring-boot/       # Java Spring Boot BaaS application
│   │   ├── src/main/      # Java controllers, services, entities, and GraphQL schema
│   │   └── pom.xml        # Maven dependencies & build configurations
│   │
│   └── rust/              # Rust Native FFI library
│       ├── src/           # Rust compression, checksum, and vector modules
│       └── Cargo.toml     # Rust package configurations
│
├── frontend/
│   └── dashboard/         # HTML SPA Console dashboard UI
│
├── apache-maven-3.9.6/    # Local Maven wrapper
├── idea.md                # Reference Architecture Blueprint Specifications
└── README.md              # Project manual (This file)
```
