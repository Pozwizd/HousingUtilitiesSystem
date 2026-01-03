#!/bin/bash

# ============================================================================
# Complete Cleanup - Remove All Containers, Volumes, Images
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "WARNING: Complete Cleanup"
echo "====================================="
echo ""
echo "This will remove:"
echo "  - All containers"
echo "  - All MongoDB data"
echo "  - All Elasticsearch data"
echo "  - All Redis data"
echo "  - All uploaded files"
echo "  - Docker images"
echo ""

read -p "Are you sure? (yes/no): " confirmation
if [[ "$confirmation" != "yes" ]]; then
    echo "Operation cancelled"
    exit 0
fi

cd "$PROJECT_DIR"

echo ""
echo "Stopping and removing all containers..."
docker-compose down -v
docker-compose -f docker-compose-services.yml down -v 2>/dev/null

echo "Removing application images..."
docker rmi housingutilitiessystemadmin-app 2>/dev/null || true

echo "Cleaning unused images..."
docker image prune -f

echo ""
echo "====================================="
echo "[SUCCESS] Complete cleanup finished"
echo "====================================="
echo ""
echo "To start fresh: $SCRIPT_DIR/all-start.sh"
