#!/bin/bash

# ============================================================================
# Stop All Services
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "Stopping All Services"
echo "====================================="
echo ""

cd "$PROJECT_DIR"

echo "Stopping all containers..."
docker-compose down
docker-compose -f docker-compose-services.yml down 2>/dev/null

echo ""
echo "====================================="
echo "[SUCCESS] All services stopped"
echo "====================================="
echo ""
echo "Data is preserved in Docker volumes"
echo "To remove all data: $SCRIPT_DIR/all-cleanup.sh"
