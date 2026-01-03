#!/bin/bash

# ============================================================================
# MongoDB Replica Set - Cleanup Script
# Очистка данных MongoDB реплик (volumes)
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "MongoDB Replica Set - Data Cleanup"
echo "====================================="
echo ""
echo "This will:"
echo "  - Stop MongoDB containers (if running)"
echo "  - Remove MongoDB containers"
echo "  - Remove all MongoDB data volumes"
echo ""
echo "⚠️  WARNING: All data will be permanently deleted!"
echo ""

read -p "Are you sure? (yes/no): " confirmation
if [[ "$confirmation" != "yes" ]]; then
    echo "Operation cancelled"
    exit 0
fi

cd "$PROJECT_DIR"

echo ""
echo "Step 1: Stopping MongoDB containers..."
docker-compose stop mongo1 mongo2 mongo3 2>/dev/null || true

echo ""
echo "Step 2: Removing MongoDB containers..."
docker-compose rm -f mongo1 mongo2 mongo3 mongo-init 2>/dev/null || true

echo ""
echo "Step 3: Removing MongoDB data volumes..."

# Удаляем volumes для данных
docker volume rm housingutilitiessystemadmin_mongo1-data 2>/dev/null && echo "  ✓ mongo1-data removed" || echo "  - mongo1-data not found"
docker volume rm housingutilitiessystemadmin_mongo2-data 2>/dev/null && echo "  ✓ mongo2-data removed" || echo "  - mongo2-data not found"
docker volume rm housingutilitiessystemadmin_mongo3-data 2>/dev/null && echo "  ✓ mongo3-data removed" || echo "  - mongo3-data not found"

# Удаляем volumes для конфигурации
docker volume rm housingutilitiessystemadmin_mongo1-config 2>/dev/null && echo "  ✓ mongo1-config removed" || echo "  - mongo1-config not found"
docker volume rm housingutilitiessystemadmin_mongo2-config 2>/dev/null && echo "  ✓ mongo2-config removed" || echo "  - mongo2-config not found"
docker volume rm housingutilitiessystemadmin_mongo3-config 2>/dev/null && echo "  ✓ mongo3-config removed" || echo "  - mongo3-config not found"

echo ""
echo "====================================="
echo "[SUCCESS] MongoDB Replica Set cleanup completed"
echo "====================================="
echo ""
echo "To start fresh: $SCRIPT_DIR/mongodb-start.sh"
echo ""
