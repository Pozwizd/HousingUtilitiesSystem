#!/bin/bash

# ============================================================================
# Elasticsearch + Kibana - Stop Script
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "Elasticsearch + Kibana - Stopping"
echo "====================================="
echo ""

cd "$PROJECT_DIR"

echo "Stopping Elasticsearch and Kibana..."
docker-compose -f docker-compose-services.yml stop elasticsearch kibana
docker-compose -f docker-compose-services.yml rm -f elasticsearch kibana

echo ""
echo "[SUCCESS] Elasticsearch + Kibana stopped"
echo ""
echo "Note: Data is preserved in Docker volumes"
