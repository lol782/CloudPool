#!/usr/bin/env bash
set -e

# Resolve scripts directory and navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "===================================================="
echo " Executing CloudPool Verification Suites            "
echo "===================================================="

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

# Run tests
$MVN_CMD clean test

echo ""
echo "Verification suites passed successfully!"
echo "===================================================="
