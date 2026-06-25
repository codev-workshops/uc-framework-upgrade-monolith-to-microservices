#!/bin/bash
# Start all microservices for local development/testing
# Usage: ./start-services.sh

set -e

echo "Building all modules..."
./gradlew :shared-contracts:build :services:user-service:bootJar :services:article-service:bootJar :services:comment-service:bootJar :services:favorite-service:bootJar :services:api-gateway:bootJar --no-daemon -x test

echo ""
echo "Starting services..."

# Start user-service first (others depend on it)
echo "Starting user-service on port 8081..."
java -jar services/user-service/build/libs/*.jar &
USER_PID=$!
sleep 5

echo "Starting article-service on port 8082..."
java -jar services/article-service/build/libs/*.jar &
ARTICLE_PID=$!

echo "Starting comment-service on port 8083..."
java -jar services/comment-service/build/libs/*.jar &
COMMENT_PID=$!

echo "Starting favorite-service on port 8084..."
java -jar services/favorite-service/build/libs/*.jar &
FAVORITE_PID=$!

sleep 5

echo "Starting api-gateway on port 8080..."
java -jar services/api-gateway/build/libs/*.jar &
GATEWAY_PID=$!

echo ""
echo "All services starting..."
echo "  user-service:     PID=$USER_PID     port=8081"
echo "  article-service:  PID=$ARTICLE_PID  port=8082"
echo "  comment-service:  PID=$COMMENT_PID  port=8083"
echo "  favorite-service: PID=$FAVORITE_PID port=8084"
echo "  api-gateway:      PID=$GATEWAY_PID  port=8080"
echo ""
echo "Press Ctrl+C to stop all services"

trap "kill $USER_PID $ARTICLE_PID $COMMENT_PID $FAVORITE_PID $GATEWAY_PID 2>/dev/null; exit" INT TERM
wait
