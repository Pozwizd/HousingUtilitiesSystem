#!/bin/bash

# ============================================================================
# Elasticsearch + Kibana - Start Script
# ============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "Elasticsearch + Kibana - Starting"
echo "====================================="
echo ""

cd "$PROJECT_DIR"

# Check Docker
if ! docker ps &> /dev/null; then
    echo "[ERROR] Docker is not running!"
    exit 1
fi

echo "Starting Elasticsearch..."
docker-compose -f docker-compose-services.yml up -d elasticsearch

echo "Waiting for Elasticsearch to be healthy (may take 1-2 minutes)..."
until curl -s http://localhost:9200/_cluster/health &> /dev/null; do
    echo -n "."
    sleep 5
done
echo ""
echo "[OK] Elasticsearch is ready"

echo "Starting Kibana..."
docker-compose -f docker-compose-services.yml up -d kibana

echo ""
echo "====================================="
echo "[SUCCESS] Elasticsearch + Kibana started!"
echo "====================================="
echo ""
echo "Services:"
echo "  - Elasticsearch: http://localhost:9200"
echo "  - Kibana:        http://localhost:5601"
echo ""
