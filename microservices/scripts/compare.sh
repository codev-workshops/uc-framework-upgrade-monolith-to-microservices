#!/bin/bash
#
# compare.sh - Start both monolith and microservices, then compare API responses
#
# Usage: ./compare.sh [monolith_port] [microservices_port]
#

set -e

MONOLITH_PORT=${1:-8080}
MICROSERVICES_PORT=${2:-9080}
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
MICROSERVICES_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=========================================="
echo "  Conduit: Monolith vs Microservices"
echo "=========================================="
echo ""
echo "Monolith URL:       http://localhost:$MONOLITH_PORT"
echo "Microservices URL:  http://localhost:$MICROSERVICES_PORT"
echo ""

# Start the monolith
echo "[1/4] Starting monolith on port $MONOLITH_PORT..."
cd "$PROJECT_ROOT"
SERVER_PORT=$MONOLITH_PORT ./gradlew bootRun &
MONOLITH_PID=$!
echo "  Monolith PID: $MONOLITH_PID"

# Start the microservices
echo "[2/4] Starting microservices on port $MICROSERVICES_PORT..."
cd "$MICROSERVICES_DIR"
# Assumes docker-compose is available and services are built
GATEWAY_PORT=$MICROSERVICES_PORT docker-compose up -d 2>/dev/null || {
    echo "  WARNING: docker-compose failed. Attempting manual start..."
    echo "  Please ensure microservices are built and docker-compose is available."
}

# Wait for services to be ready
echo "[3/4] Waiting for services to start..."
echo "  Waiting for monolith..."
for i in $(seq 1 60); do
    if curl -s "http://localhost:$MONOLITH_PORT/tags" > /dev/null 2>&1; then
        echo "  Monolith is ready!"
        break
    fi
    if [ $i -eq 60 ]; then
        echo "  ERROR: Monolith failed to start within 60 seconds"
        kill $MONOLITH_PID 2>/dev/null
        exit 1
    fi
    sleep 1
done

echo "  Waiting for microservices..."
for i in $(seq 1 60); do
    if curl -s "http://localhost:$MICROSERVICES_PORT/tags" > /dev/null 2>&1; then
        echo "  Microservices are ready!"
        break
    fi
    if [ $i -eq 60 ]; then
        echo "  WARNING: Microservices may not be fully ready"
    fi
    sleep 1
done

# Run API comparison
echo "[4/4] Running API comparison..."
echo ""
"$SCRIPT_DIR/api-compare.sh" "$MONOLITH_PORT" "$MICROSERVICES_PORT"

# Cleanup
echo ""
echo "Cleaning up..."
kill $MONOLITH_PID 2>/dev/null || true
cd "$MICROSERVICES_DIR" && docker-compose down 2>/dev/null || true

echo "Done!"
