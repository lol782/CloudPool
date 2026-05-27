# 🚀 CloudPool

> **A Developer Infrastructure Orchestration Platform with Versioned State, Provider Abstraction, and Secure Local-to-Cloud Connectivity**

## 📌 Overview
CloudPool is a developer-focused infrastructure orchestration system that allows you to:
- Connect and manage local and cloud databases.
- Expose local services securely over the internet (ngrok-like tunneling).
- Use versioned persistence and rollback (Git-like infra state).
- Integrate external providers like PostgreSQL, Redis, and storage systems.
- Maintain a unified control plane for developer infrastructure.

Instead of replacing cloud providers, CloudPool acts as a unified control layer over your infrastructure.

---

## 🧠 The Problem It Solves

Modern developers use fragmented tools:
- PostgreSQL (local / cloud)
- Redis instances
- File storage (Google Drive / S3)
- Local dev environments
- Multiple config systems
- No unified rollback or infra state tracking

**CloudPool solves this by introducing a single orchestration layer for managing developer infrastructure state.**

---

## ⚙️ Core Features

### 🔐 1. Authentication System
- Spring Boot-based secure login system.
- User-scoped infrastructure configurations.

### 🗄️ 2. Database Orchestration Layer
- Connect external PostgreSQL and Redis instances.
- Execute queries via a unified console.
- Switch between local and external databases seamlessly.

### 📦 3. Versioned Infrastructure State
- Snapshot system for database state, configuration state, and storage mappings.
- Rollback to previous working states.
- Git-like infrastructure versioning concept.

### ☁️ 4. Storage Integration (Google Drive Backend)
- Upload/download files via Google Drive API.
- Metadata tracking layer.
- File abstraction system over cloud storage.

### 🔌 5. Provider Abstraction Layer
- **Supports multiple backend providers:** PostgreSQL (local + remote), Redis (remote), Google Drive (storage layer).
- **Designed to be extensible to:** S3, Neon DB, Upstash, and any REST/gRPC-based infrastructure.

### 🌐 6. Secure Tunneling System (Agent-Based Design - *Planned*)
- Desktop agent (CloudPool Agent).
- Expose local services securely (ngrok-like tunneling mechanism).
- Connect local PostgreSQL / Redis to CloudPool.

### 📊 7. Unified Developer Dashboard
- Manage infrastructure connections and view active services.
- Execute queries (SQL + Redis CLI style).
- Monitor system state.

---

## 🧩 Tech Stack

| Component | Technology |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot, Spring Security, JPA / Hibernate |
| **Database** | PostgreSQL (user-managed or external), H2 (fallback local DB) |
| **Cache / NoSQL** | Redis integration via Jedis |
| **Storage** | Google Drive API |
| **Future Additions** | GraphQL API layer, Vector DB support (Weaviate / Milvus), Tunnel agent (Rust/Go desktop client) |

---

## 🖥️ API Overview

### Authentication
```http
POST /api/auth/signup
POST /api/auth/login
