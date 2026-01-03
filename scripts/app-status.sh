#!/bin/bash

# ============================================================================
# Spring Boot Application - Status Script
# ============================================================================

echo "====================================="
echo "Application - Status"
echo "====================================="
echo ""

echo "Container:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(housing-utilities-app|NAMES)" || echo "Application not running"

echo ""
echo "Health Check:"
curl -s http://localhost:8080/actuator/health 2>/dev/null || echo "  Application not available"
echo ""
