#!/usr/bin/env bash
set -e

# Resolve scripts directory and navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "===================================================="
echo " Starting CloudPool Platform Setup (Unix/Linux/macOS) "
echo "===================================================="

# 1. Compile native Rust module
echo ""
echo "[1/2] Compiling Native Rust Modules (FFI)..."
cd "${ROOT_DIR}/backend/rust"
if ! command -v cargo &> /dev/null; then
    echo "ERROR: Rust compiler 'cargo' was not found on your PATH."
    exit 1
fi
cargo build --release
echo "Native Rust modules compiled successfully!"

# 2. Build Spring Boot Java project
echo ""
echo "[2/2] Resolving Maven and Compiling Spring Boot Backend..."
cd "${ROOT_DIR}/backend/spring-boot"

# Resolve maven executable
MVN_CMD="mvn"
if ! command -v mvn &> /dev/null; then
    if [ -f "${ROOT_DIR}/apache-maven-3.9.6/bin/mvn" ]; then
        MVN_CMD="${ROOT_DIR}/apache-maven-3.9.6/bin/mvn"
        echo "Using bundled Maven at: ${MVN_CMD}"
    else
        echo "ERROR: Maven ('mvn') was not found on your PATH and no bundled maven was found."
        exit 1
    fi
fi

$MVN_CMD clean install -DskipTests
echo "Spring Boot Java backend compiled successfully!"

echo ""
echo "===================================================="
echo " Setup complete! CloudPool is ready for launch.     "
echo "===================================================="
