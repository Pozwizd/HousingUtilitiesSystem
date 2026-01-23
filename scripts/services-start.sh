#!/bin/bash

# ============================================================================
# Local Services (Redis + Elasticsearch) - Start Script
# Для локальной разработки: запускает только сервисы без приложений
# ============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "Local Dev Services - Starting"
echo "====================================="
echo ""

cd "$PROJECT_DIR"

# Check Docker
if ! docker ps &> /dev/null; then
    echo "[ERROR] Docker is not running!"
    echo "Please start Docker Desktop and try again."
    exit 1
fi

# Check docker-compose file
if [ ! -f "docker-compose-services.yml" ]; then
    echo "[ERROR] docker-compose-services.yml not found in $PROJECT_DIR"
    exit 1
fi

echo "[1/2] Starting Redis..."
docker-compose -f docker-compose-services.yml up -d redis
echo "[OK] Redis container started"

echo ""
echo "[2/2] Starting Elasticsearch..."
docker-compose -f docker-compose-services.yml up -d elasticsearch

echo ""
echo "Waiting for Elasticsearch to be ready (may take 1-2 minutes)..."
COUNTER=0
MAX_TRIES=30
until curl -s http://localhost:9200/_cluster/health &> /dev/null; do
    echo -n "."
    sleep 5
    COUNTER=$((COUNTER + 1))
    if [ $COUNTER -ge $MAX_TRIES ]; then
        echo ""
        echo "[WARNING] Elasticsearch is taking longer than expected."
        echo "Check logs: docker logs elasticsearch-services"
        break
    fi
done
echo ""
echo "[OK] Elasticsearch is ready"

echo ""
echo "====================================="
echo "[SUCCESS] Local Dev Services Started!"
echo "====================================="
echo ""
echo "Services available:"
echo "  - Redis:         localhost:6379"
echo "  - Elasticsearch: http://localhost:9200"
echo ""
echo "Now you can run Spring Boot apps from IDE with profile: dev"
echo ""
echo "Useful commands:"
echo "  - Stop services:     ./scripts/services-stop.sh"
echo "  - Check status:      ./scripts/services-status.sh"
echo "  - View Redis logs:   docker logs redis-services"
echo "  - View ES logs:      docker logs elasticsearch-services"
echo ""
