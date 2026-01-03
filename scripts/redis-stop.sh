#!/bin/bash

# ============================================================================
# Redis + Redis Commander - Stop Script
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "Redis - Stopping"
echo "====================================="
echo ""

cd "$PROJECT_DIR"

echo "Stopping Redis and Redis Commander..."
docker-compose -f docker-compose-services.yml stop redis redis-commander
docker-compose -f docker-compose-services.yml rm -f redis redis-commander

echo ""
echo "[SUCCESS] Redis stopped"
echo ""
echo "Note: Data is preserved in Docker volumes"
