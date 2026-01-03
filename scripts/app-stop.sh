#!/bin/bash

# ============================================================================
# Spring Boot Application - Stop Script
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "Application - Stopping"
echo "====================================="
echo ""

cd "$PROJECT_DIR"

echo "Stopping application..."
docker-compose stop app
docker-compose rm -f app

echo ""
echo "[SUCCESS] Application stopped"
echo ""
echo "Note: MongoDB and other services are still running"
echo "To stop all: $SCRIPT_DIR/all-stop.sh"
