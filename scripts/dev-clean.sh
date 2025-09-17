#!/bin/bash

# Development Environment Clean Script
# This script completely removes all containers, volumes, and networks

set -e

echo "🧹 Cleaning Videos API Development Environment..."

# Stop and remove all containers, volumes, and networks
docker-compose down -v --remove-orphans

# Remove any dangling images
echo "🗑️ Removing dangling Docker images..."
docker image prune -f

echo "✅ Development environment cleaned completely."
echo ""
echo "🚀 To start fresh: ./scripts/dev-setup.sh"
