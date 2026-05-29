<div align="center">
  <h1>CloudPool</h1>
  <p><strong>Developer Infrastructure Orchestration & Decentralized BaaS Platform</strong></p>
  
  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
  [![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://java.com/)
  [![Rust](https://img.shields.io/badge/Rust-1.70+-black.svg)](https://www.rust-lang.org/)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
</div>

<br/>

## Overview

CloudPool is a high-performance, open-source Developer Infrastructure Orchestration Platform designed to unify backend services. By consolidating storage pooling, dynamic database provisioning, and vector search indexing into a single orchestration layer, CloudPool acts as a unified control plane over your local and cloud infrastructure.

Rather than replacing existing cloud providers, CloudPool seamlessly integrates them, providing developers with versioned infrastructure state tracking, secure local-to-cloud connectivity, and native Rust-powered performance extensions.

## Core Capabilities

- **Unified Infrastructure Orchestration:** Manage dynamic PostgreSQL instances, Redis caches, and local H2 environments from a single developer console.
- **Hybrid Storage Pooling:** Connect external storage providers (e.g., Google Drive) or utilize high-performance local disk fallback. Includes secure, expirable access token management.
- **Native Performance Layer:** Critical execution paths—including SHA-256 checksum operations, Gzip/zstd compression, and vector mathematics—are implemented in Rust and invoked via Java Native Interface (JNI) for minimal latency.
- **Dynamic Provisioning Engine:** Dynamically provision database tables, define schemas, and execute raw SQL operations without requiring application restarts.
- **Infrastructure Rollback:** Maintain versioned snapshots of database schemas and configuration state to enable real-time infrastructure rollbacks.
- **Embedded Vector Search:** Out-of-the-box semantic text chunk indexing utilizing Weaviate, with a local cosine-similarity fallback powered by Rust.

## Architecture

CloudPool implements a hybrid polyglot architecture to maximize both developer productivity and raw execution speed:

- **Orchestration Layer (Java/Spring Boot 3.x):** Handles complex business logic, multi-protocol API routing (GraphQL/REST), user scoping, and secure multi-tenant isolation.
- **Native FFI Module (Rust):** A compiled dynamic library strictly designed for CPU-bound computations and zero-copy data transformations.
- **Frontend Dashboard (Vanilla JS):** A lightweight, zero-build-step Single Page Application (SPA) providing the developer console interface.

For an exhaustive architectural deep dive, please refer to the [Architecture Blueprint](idea.md).

## Getting Started

### Prerequisites

To compile and run CloudPool from source, ensure the following dependencies are installed on your host system:

- **Java Development Kit (JDK):** Version 17 or 21
- **Rust Toolchain:** Version 1.70 or higher (including `cargo`)
- **Maven:** A local wrapper (`apache-maven-3.9.6`) is provided in the repository.

### Build Instructions

**1. Compile the Native Rust Library**

The core Spring Boot application dynamically loads a compiled Rust binary at runtime.

```bash
cd backend/rust
cargo build --release
Note: This generates the shared object/dynamic library (.so, .dll, or .dylib) in the target/release/ directory.

2. Verify FFI Integration

Before running the application, validate the JNI bindings between the JVM and the compiled Rust module.

cd ../spring-boot
../../apache-maven-3.9.6/bin/mvn exec:java -Dexec.mainClass="com.cloudpool.util.JniTest"
3. Initialize the Application

Run the Spring Boot application using the default local profile, which utilizes standalone H2 databases and local vector search.

../../apache-maven-3.9.6/bin/mvn spring-boot:run -Dspring-boot.run.profiles=local
Accessing the Platform
Upon successful startup, the following interfaces are available:

Developer Console: http://localhost:8080/index.html
GraphQL Endpoint: http://localhost:8080/graphql
GraphiQL Playground: http://localhost:8080/graphiql
H2 Database Console: http://localhost:8080/h2-console
Documentation
Comprehensive documentation covering REST API specifications, GraphQL schemas, database design, and deployment methodologies can be found in the docs/ directory and the main Implementation Guide.

Contributing
We welcome contributions from the open-source community. Whether you are addressing bug fixes, optimizing the Rust FFI layer, or enhancing the frontend console, please review our Contributing Guidelines to understand our development workflow, coding standards, and pull request process.

Please ensure all commits adhere to the Conventional Commits specification.

License
CloudPool is licensed under the Apache License 2.0.
