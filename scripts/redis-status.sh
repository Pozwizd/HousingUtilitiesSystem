#!/bin/bash

# ============================================================================
# Redis - Status Script
# ============================================================================

echo "====================================="
echo "Redis - Status"
echo "====================================="
echo ""

echo "Containers:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(redis|NAMES)" || echo "No Redis containers running"

echo ""
echo "Redis Info:"
docker exec redis-services redis-cli INFO server 2>/dev/null | head -10 || echo "  Redis not available"
echo ""
