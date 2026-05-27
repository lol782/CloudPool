🚀 CloudPool
A Developer Infrastructure Orchestration Platform with Versioned State, Provider Abstraction, and Secure Local-to-Cloud Connectivity

📌 Overview
CloudPool is a developer-focused infrastructure orchestration system that allows you to:
+Connect and manage local + cloud databases
+Expose local services securely over the internet (ngrok-like tunneling)
+Use versioned persistence and rollback (Git-like infra state)
+Integrate external providers like PostgreSQL, Redis, and storage systems
+Maintain a unified control plane for developer infrastructure

Instead of replacing cloud providers, CloudPool acts as a control layer over your infrastructure.

🧠 Problem It Solves:
#Modern developers use fragmented tools:
+PostgreSQL (local / cloud)
+Redis instances
+File storage (Google Drive / S3)
+Local dev environments
+Multiple config systems
+No unified rollback or infra state tracking

CloudPool solves this by introducing:
A single orchestration layer for managing developer infrastructure state.

⚙️ Core Features
🔐 1. Authentication System
Spring Boot-based secure login system
User-scoped infrastructure configurations

🗄️2. Database Orchestration Layer
Connect external PostgreSQL instances
Connect Redis instances
Execute queries via unified console
Switch between local and external DB seamlessly

📦3. Versioned Infrastructure State
Snapshot system for:
Database state
Configuration state
Storage mappings
Rollback to previous working states
Git-like infra versioning concept

☁️4. Storage Integration (Google Drive Backend)
Upload / download files via Google Drive API
Metadata tracking layer
File abstraction system over cloud storage

🔌5. Provider Abstraction Layer
#Supports multiple backend providers:
+PostgreSQL (local + remote)
+Redis (remote instances)
+Google Drive (storage layer)

#Designed to be extensible to:
*S3
*Neon DB
*Upstash
*Any REST/gRPC-based infra

🌐 6. Secure Tunneling System (Agent-Based Design)
#Planned feature:
*Desktop agent (CloudPool Agent)
*Expose local services securely
*ngrok-like tunneling mechanism
*Connect local PostgreSQL / Redis to CloudPool

📊 7. Unified Developer Dashboard
+Manage infrastructure connections
+View active services
+Execute queries (SQL + Redis CLI style)
+Monitor system state


🧩 Tech Stack
Backend
Java 21
Spring Boot
Spring Security
JPA / Hibernate
Database
PostgreSQL (user-managed or external)
H2 (fallback local DB)
Cache / NoSQL
Redis integration via Jedis
Storage
Google Drive API
Future Additions
GraphQL API layer
Vector DB support (Weaviate / Milvus)
Tunnel agent (Rust/Go desktop client)

🖥️ API Overview
Authentication
POST /api/auth/signup
POST /api/auth/login
Database Connections
POST   /api/v1/db/connections
GET    /api/v1/db/connections
DELETE /api/v1/db/connections/{id}
POST   /api/v1/db/connections/test
SQL Console
POST /api/v1/db/execute
Redis Console
POST /api/v1/redis/execute
File Storage (Google Drive)
POST /api/v1/storage/upload
GET  /api/v1/storage/download/{id}
Snapshot System
POST /api/v1/snapshots/create
GET  /api/v1/snapshots
POST /api/v1/snapshots/rollback/{id}

🧪 Example Use Cases

1. Developer Backend Testing Platform
Connect your PostgreSQL
Run SQL queries via dashboard
Rollback schema states

2. Local Dev Exposure
Run PostgreSQL locally
Expose via CloudPool Agent (future)
Access from anywhere securely

3. Cloud Storage Layer
Upload files to Google Drive via unified API
Maintain metadata and version history

4. Infrastructure Snapshotting
Save full project state
Restore broken environments instantly

🚧 Current Status
Implemented
Authentication system (Spring Security)
Google Drive integration
File upload/download system
PostgreSQL connection layer (external support started)
Redis command execution (basic)
Basic UI dashboard
In Progress
Snapshot/versioning system
Provider abstraction layer cleanup
Redis console improvements
Planned
CloudPool Desktop Agent (ngrok-like)
Full infra tunneling system
GraphQL API layer
Plugin system for providers
CLI tool

🧠 Design Philosophy
CloudPool is NOT:
❌ A cloud provider
❌ An AWS replacement
❌ A distributed database system

CloudPool IS:

✅ A developer infrastructure control plane
✅ A state/version management system for backend services
✅ A connectivity layer between local and cloud infra
📌 Why This Project Exists

To explore:
Infrastructure orchestration
Developer tooling abstraction
State versioning systems
Secure local-to-cloud connectivity
Backend system design patterns

🤝 Contributing
We welcome contributions in areas like:
#Backend architecture improvements
#Provider integrations (PostgreSQL, Redis, S3, etc.)
#UI/UX enhancements
#Tunnel agent development
#Snapshot/versioning engine improvements

⚠️ Disclaimer
This project is experimental and designed for learning and developer tooling exploration. It is not intended for production-critical workloads at this stage.

⭐ Future Vision
CloudPool aims to evolve into a:
#Universal Developer Infrastructure Orchestration Layer 
#bridging local development environments with cloud-native systems in a unified, versioned, and controllable way.
