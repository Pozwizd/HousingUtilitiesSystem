#!/bin/bash

# ============================================================================
# MongoDB Replica Set - Status Script
# ============================================================================

echo "====================================="
echo "MongoDB Replica Set - Status"
echo "====================================="
echo ""

echo "Containers:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(mongo|NAMES)" || echo "No MongoDB containers running"

echo ""
echo "Replica Set Status:"
docker exec mongo1 mongosh --quiet --eval "rs.status().members.forEach(m => print('  ' + m.name + ' - ' + m.stateStr))" 2>/dev/null || echo "  Replica Set not available"
echo ""
