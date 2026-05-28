#!/usr/bin/env bash
set -e

# Resolve scripts directory and navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "===================================================="
echo " Deploying Local Development Services (Docker)      "
echo "===================================================="

cd "${ROOT_DIR}"

# Check for docker-compose command support
COMPOSE_CMD="docker-compose"
if ! command -v docker-compose &> /dev/null; then
    if docker compose version &> /dev/null; then
        COMPOSE_CMD="docker compose"
    else
        echo "ERROR: Docker Compose ('docker-compose' or 'docker compose') was not found on your system."
        exit 1
    fi
fi

# Bring up the environment
echo "Starting containers..."
$COMPOSE_CMD up -d

echo ""
echo "Verifying service status..."
$COMPOSE_CMD ps

echo ""
echo "Development environment services deployed successfully!"
echo "===================================================="
