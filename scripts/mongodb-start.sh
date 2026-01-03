#!/bin/bash

# ============================================================================
# MongoDB Replica Set - Start Script
# ============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "====================================="
echo "MongoDB Replica Set - Starting"
echo "====================================="
echo ""

cd "$PROJECT_DIR"

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "[ERROR] Docker not found!"
    exit 1
fi

if ! docker ps &> /dev/null; then
    echo "[ERROR] Docker is not running!"
    exit 1
fi

echo "[OK] Docker is running"
echo ""

# Start MongoDB Replica Set
echo "Starting MongoDB Replica Set (3 nodes)..."
docker-compose up -d mongo1 mongo2 mongo3

# Wait for healthy state
echo "Waiting for MongoDB nodes to be healthy..."
sleep 10

# Initialize Replica Set
echo "Initializing Replica Set..."
docker-compose up mongo-init

echo ""
echo "====================================="
echo "[SUCCESS] MongoDB Replica Set started!"
echo "====================================="
echo ""
echo "Nodes:"
echo "  - mongo1 (PRIMARY):   localhost:27017"
echo "  - mongo2 (SECONDARY): localhost:27018"
echo "  - mongo3 (SECONDARY): localhost:27019"
echo ""
