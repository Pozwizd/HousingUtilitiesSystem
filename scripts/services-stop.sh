#!/bin/bash

# ============================================================================
# Local Services (Redis + Elasticsearch) - Stop Script
# ============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "Local Dev Services - Stopping"
echo "====================================="
echo ""

cd "$PROJECT_DIR"

# Check Docker
if ! docker ps &> /dev/null; then
    echo "[ERROR] Docker is not running!"
    exit 1
fi

echo "Stopping services..."
docker-compose -f docker-compose-services.yml down

echo ""
echo "====================================="
echo "[SUCCESS] Local Dev Services Stopped!"
echo "====================================="
echo ""
