#!/bin/bash

# ============================================================================
# Spring Boot Application - Logs Script
# ============================================================================

echo "====================================="
echo "Application Logs"
echo "====================================="
echo "Press Ctrl+C to exit"
echo ""

docker logs -f housing-utilities-app
