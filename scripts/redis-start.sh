#!/bin/bash

# ============================================================================
# Redis + Redis Commander - Start Script
# ============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "Redis - Starting"
echo "====================================="
echo ""

cd "$PROJECT_DIR"

# Check Docker
if ! docker ps &> /dev/null; then
    echo "[ERROR] Docker is not running!"
    exit 1
fi

echo "Starting Redis..."
docker-compose -f docker-compose-services.yml up -d redis

echo "Waiting for Redis..."
sleep 3

echo "Starting Redis Commander (Web UI)..."
docker-compose -f docker-compose-services.yml up -d redis-commander

echo ""
echo "====================================="
echo "[SUCCESS] Redis started!"
echo "====================================="
echo ""
echo "Services:"
echo "  - Redis:           localhost:6379"
echo "  - Redis Commander: http://localhost:8081"
echo ""
