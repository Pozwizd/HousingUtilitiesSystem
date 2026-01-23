#!/bin/bash

# ============================================================================
# Local Services (Redis + Elasticsearch) - Status Script
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "Local Dev Services - Status"
echo "====================================="
echo ""

cd "$PROJECT_DIR"

# Check Docker
if ! docker ps &> /dev/null; then
    echo "[ERROR] Docker is not running!"
    exit 1
fi

echo "Container Status:"
echo "-----------------"
docker-compose -f docker-compose-services.yml ps

echo ""
echo "Health Checks:"
echo "--------------"

# Redis check
if docker exec redis-services redis-cli ping &> /dev/null; then
    echo "[OK] Redis: healthy (localhost:6379)"
else
    echo "[--] Redis: not running"
fi

# Elasticsearch check
if curl -s http://localhost:9200/_cluster/health &> /dev/null; then
    STATUS=$(curl -s http://localhost:9200/_cluster/health | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    echo "[OK] Elasticsearch: $STATUS (http://localhost:9200)"
else
    echo "[--] Elasticsearch: not running"
fi

echo ""
