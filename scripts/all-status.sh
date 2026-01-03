#!/bin/bash

# ============================================================================
# Status of All Services
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "====================================="
echo "All Services - Status"
echo "====================================="
echo ""

echo "=== Docker Containers ==="
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "=== MongoDB Replica Set ==="
docker exec mongo1 mongosh --quiet --eval "rs.status().members.forEach(m => print('  ' + m.name + ' - ' + m.stateStr))" 2>/dev/null || echo "  Not running"

echo ""
echo "=== Elasticsearch ==="
curl -s http://localhost:9200/_cluster/health 2>/dev/null | grep -o '"status":"[^"]*"' || echo "  Not running"

echo ""
echo "=== Redis ==="
docker exec redis-services redis-cli PING 2>/dev/null || echo "  Not running"

echo ""
echo "=== Application ==="
curl -s http://localhost:8080/actuator/health 2>/dev/null || echo "  Not running"
echo ""
