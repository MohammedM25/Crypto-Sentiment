#!/bin/bash

# Crypto Sentiment - Local Development Startup Script

echo "🚀 Starting Crypto Sentiment Platform Locally..."
echo ""

# Check if Docker is running
if ! docker ps > /dev/null 2>&1; then
    echo "❌ Docker is not running!"
    echo "   Please start Docker Desktop first, then run this script again."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Start database
echo "📦 Starting PostgreSQL database..."
cd "$(dirname "$0")/infra"
docker-compose up -d

# Wait for database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 5

# Check if database is running
if docker ps | grep -q crypto-postgres; then
    echo "✅ Database is running"
else
    echo "❌ Failed to start database"
    exit 1
fi

echo ""
echo "🎯 Next steps:"
echo "   1. Backend: cd backend/crypto-backend && ./gradlew bootRun"
echo "   2. Frontend: cd frontend && npm start"
echo ""
echo "   Or run them in separate terminals!"
echo ""


