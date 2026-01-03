#!/bin/bash

# ============================================================================
# MongoDB Replica Set - Stop Script
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "MongoDB Replica Set - Stopping"
echo "====================================="
echo ""

cd "$PROJECT_DIR"

echo "Stopping MongoDB containers..."
docker-compose stop mongo1 mongo2 mongo3
docker-compose rm -f mongo1 mongo2 mongo3 mongo-init

echo ""
echo "[SUCCESS] MongoDB Replica Set stopped"
echo ""
echo "Note: Data is preserved in Docker volumes"
echo "To remove data: docker volume rm housingutilitiessystemadmin_mongo1-data housingutilitiessystemadmin_mongo2-data housingutilitiessystemadmin_mongo3-data"
