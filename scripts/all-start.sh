#!/bin/bash

# ============================================================================
# Start All Services
# ============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "====================================="
echo "Starting All Services"
echo "====================================="
echo ""

# 1. MongoDB
echo ">>> Starting MongoDB Replica Set..."
"$SCRIPT_DIR/mongodb-start.sh"

# 2. Elasticsearch (optional)
read -p "Start Elasticsearch? (y/n): " start_es
if [[ "$start_es" == "y" ]]; then
    "$SCRIPT_DIR/elasticsearch-start.sh"
fi

# 3. Redis (optional)
read -p "Start Redis? (y/n): " start_redis
if [[ "$start_redis" == "y" ]]; then
    "$SCRIPT_DIR/redis-start.sh"
fi

# 4. Application
echo ""
echo ">>> Starting Application..."
"$SCRIPT_DIR/app-start.sh"

echo ""
echo "====================================="
echo "[SUCCESS] All services started!"
echo "====================================="
echo ""
echo "Services:"
echo "  - Application:    http://localhost:8080"
echo "  - MongoDB:        localhost:27017"
[[ "$start_es" == "y" ]] && echo "  - Elasticsearch:  http://localhost:9200"
[[ "$start_es" == "y" ]] && echo "  - Kibana:         http://localhost:5601"
[[ "$start_redis" == "y" ]] && echo "  - Redis:          localhost:6379"
[[ "$start_redis" == "y" ]] && echo "  - Redis Commander: http://localhost:8081"
echo ""
