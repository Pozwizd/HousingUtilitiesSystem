#!/bin/bash

# ============================================================================
# Spring Boot Application - Start Script
# ============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "Application - Starting"
echo "====================================="
echo ""

cd "$PROJECT_DIR"

# Check Docker
if ! docker ps &> /dev/null; then
    echo "[ERROR] Docker is not running!"
    exit 1
fi

# Check if MongoDB is running
if ! docker ps | grep -q mongo1; then
    echo "[WARNING] MongoDB is not running. Starting MongoDB first..."
    "$SCRIPT_DIR/mongodb-start.sh"
fi

echo ""
echo "Building and starting application..."
docker-compose up --build -d app

echo ""
echo "====================================="
echo "[SUCCESS] Application started!"
echo "====================================="
echo ""
echo "Service:"
echo "  - Application: http://localhost:8080"
echo ""
echo "Waiting for application to be ready (may take 1-2 minutes)..."
echo "Monitor logs: $SCRIPT_DIR/app-logs.sh"
echo ""
