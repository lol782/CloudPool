# CloudPool Local Development Manual

This document provides detailed guidelines for setting up your local environment, building the native Rust core, starting background services, and running tests.

---

## 🛠️ System Prerequisites

### Minimum System Specifications
* **Operating System**: Linux (Ubuntu 20.04+), macOS (10.15+), or Windows 10/11
* **RAM**: 8 GB minimum (16 GB recommended)
* **Disk Space**: 50 GB of free space
* **Internet**: Stable connection for dependency resolution and API operations

### Required Software Components
* **Java Development Kit (JDK)**: JDK 17 or higher
* **Maven**: Version 3.8.1 or higher
* **Rust & Cargo**: Version 1.70+
* **Docker & Docker Compose**: Docker 20.10+ / Compose 2.0+
* **PostgreSQL Client**: Version 15+
* **Git**: Version 2.30+

---

## 🚀 Step-by-Step Installation Guide

### 1. Java Development Kit (JDK 17) Setup

> [!IMPORTANT]
> Ensure that your `JAVA_HOME` environment variable points to the JDK 17 directory.

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-17-jdk openjdk-17-jre
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
source ~/.bashrc
java -version
```

#### macOS (via Homebrew)
```bash
brew install openjdk@17
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
source ~/.zshrc
java -version
```

### 2. Maven Setup

#### Linux (Ubuntu/Debian)
```bash
sudo apt install maven
mvn -version
```

#### macOS (via Homebrew)
```bash
brew install maven
mvn -version
```

#### Recommended User Maven Settings (`~/.m2/settings.xml`)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.1.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 http://maven.apache.org/xsd/settings-1.1.0.xsd">
  <localRepository>${user.home}/.m2/repository</localRepository>
  <mirrors>
    <mirror>
      <id>central-mirror</id>
      <name>Maven Central Mirror</name>
      <url>https://repo.maven.apache.org/maven2/</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

### 3. Rust & Cargo Toolchain Setup
Install the toolchain using the official installer:

```bash
# Download and install rustup
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh

# Configure terminal environment
source $HOME/.cargo/env

# Verify toolchain version
rustc --version
cargo --version
```

---

## 🐳 Running Local Infrastructures (Docker Compose)

The environment runs inside Docker Compose. Ensure you have copied `.env.example` to `.env` in the root workspace.

### Starting Services
Start PostgreSQL, Redis, Weaviate, RabbitMQ, Prometheus, Grafana, and Jaeger:

```bash
# Navigate to workspace root
cd cloudpool

# Launch containers in background
docker-compose up -d

# Verify that all containers are healthy
docker-compose ps
```

### Port Mappings & Services Overview

| Service Name | Port | Description |
| :--- | :--- | :--- |
| **PostgreSQL** | `5432` | Relational Storage database |
| **Redis** | `6379` | Cache manager and storage key repository |
| **Weaviate** | `8081` | Vector engine storage |
| **RabbitMQ** | `5672` / `15672` | AMQP Queue broker & management console |
| **Prometheus** | `9090` | Metrics storage engine |
| **Grafana** | `3000` | Analytics visualization board (admin/admin) |
| **Jaeger** | `16686` | Distributed trace logger UI |

---

## 🏗️ Building and Compiling the Platform

### 1. Compile the Native FFI Library (Rust)
The Spring Boot server relies on the native compiled Rust shared library (`cloudpool_rust`) to compute checksums and process high-speed compression.

```bash
# Navigate to Rust folder
cd backend/rust

# Build the release shared library
cargo build --release
```

The resulting library will be output at:
* **Windows**: `backend/rust/target/release/cloudpool_rust.dll`
* **Linux**: `backend/rust/target/release/libcloudpool_rust.so`
* **macOS**: `backend/rust/target/release/libcloudpool_rust.dylib`

### 2. Initialize Database & Run Migrations
Run the initial DDL schemas and background job structure updates via Flyway:

```bash
# Navigate to spring-boot folder
cd backend/spring-boot

# Run flyway migrations against the postgres container
./mvnw flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/cloudpool \
  -Dflyway.user=cloudpool \
  -Dflyway.password=your_secure_password_here
```

### 3. Run the Spring Boot Server
You can launch the server using Maven:

```bash
# Navigate to backend/spring-boot
./mvnw spring-boot:run
```

The application starts on port `8080`.
* **GraphQL Playgrounds (GraphiQL)**: `http://localhost:8080/graphiql`
* **REST Base API**: `http://localhost:8080/api`

---

## 🧪 Testing Strategy
To verify correctness of both units and controller layers, run the maven verification suites:

```bash
# Run unit and integration tests
./mvnw clean test
```
