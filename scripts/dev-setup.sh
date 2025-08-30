#!/bin/bash

# Development Environment Setup Script
# This script sets up the complete development environment with Docker

set -e

echo "🚀 Setting up Videos API Development Environment..."

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file from .env.example..."
    cp .env.example .env
    echo "✅ .env file created. Please review and update if needed."
fi

# Start infrastructure services only (no app)
echo "🐳 Starting infrastructure services (PostgreSQL, Redis, Kafka, Azurite)..."
docker-compose up -d postgres redis zookeeper kafka kafka-ui azurite

# Wait for services to be healthy
echo "⏳ Waiting for services to be ready..."
sleep 30

# Check service health
echo "🔍 Checking service health..."
docker-compose ps

echo ""
echo "✅ Development environment is ready!"
echo ""
echo "📋 Available services:"
echo "   • PostgreSQL: localhost:5432"
echo "   • Redis: localhost:6379"
echo "   • Kafka: localhost:9092"
echo "   • Kafka UI: http://localhost:8081"
echo "   • Azurite (Mock Azure Storage): localhost:10000"
echo ""
echo "🏃 To run the application:"
echo "   • From IDE: Set SPRING_PROFILES_ACTIVE=local and run VideosApiApplication"
echo "   • With Docker: docker-compose --profile full-stack up"
echo ""
echo "🛑 To stop services: ./scripts/dev-stop.sh"
