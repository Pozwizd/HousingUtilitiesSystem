#!/bin/bash

# ============================================================================
# Elasticsearch - Status Script
# ============================================================================

echo "====================================="
echo "Elasticsearch - Status"
echo "====================================="
echo ""

echo "Containers:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(elastic|kibana|NAMES)" || echo "No Elasticsearch containers running"

echo ""
echo "Cluster Health:"
curl -s http://localhost:9200/_cluster/health?pretty 2>/dev/null || echo "  Elasticsearch not available"
echo ""
