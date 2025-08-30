#!/bin/bash

# Development Environment Stop Script
# This script stops all development services

set -e

echo "🛑 Stopping Videos API Development Environment..."

# Stop all services
docker-compose down

echo "✅ All services stopped."
echo ""
echo "💡 To remove volumes and clean up completely:"
echo "   docker-compose down -v"
echo ""
echo "🚀 To start again: ./scripts/dev-setup.sh"
