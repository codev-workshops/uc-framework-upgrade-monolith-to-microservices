#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
RESULTS_DIR="$ROOT_DIR/results"

mkdir -p "$RESULTS_DIR"

echo "=== RealWorld Conduit: Monolith vs Microservices Comparison ==="
echo ""

# Step 1: Start monolith
echo "--- Step 1: Starting monolith ---"
cd "$REPO_ROOT"
./gradlew bootRun &
MONOLITH_PID=$!
sleep 15

cd "$REPO_ROOT/frontend"
npm start &
FRONTEND_PID=$!
sleep 10

# Step 2: Run tests against monolith
echo "--- Step 2: Running E2E tests against monolith ---"
cd "$ROOT_DIR/e2e-tests"
API_URL=http://localhost:8080 BASE_URL=http://localhost:3000 npx playwright test --reporter=json > "$RESULTS_DIR/monolith-report.json" 2>&1 || true

# Step 3: Stop monolith
echo "--- Step 3: Stopping monolith ---"
kill $FRONTEND_PID 2>/dev/null || true
kill $MONOLITH_PID 2>/dev/null || true
sleep 5

# Step 4: Start microservices
echo "--- Step 4: Starting microservices ---"
cd "$ROOT_DIR"
docker-compose up -d
echo "Waiting for services to be healthy..."
sleep 30

# Step 5: Run tests against microservices
echo "--- Step 5: Running E2E tests against microservices ---"
cd "$ROOT_DIR/e2e-tests"
API_URL=http://localhost:8080 BASE_URL=http://localhost:3000 npx playwright test --reporter=json > "$RESULTS_DIR/microservices-report.json" 2>&1 || true

# Step 6: Stop microservices
echo "--- Step 6: Stopping microservices ---"
cd "$ROOT_DIR"
docker-compose down

# Step 7: Compare results
echo "--- Step 7: Comparing results ---"
echo ""
echo "Monolith results:"
cat "$RESULTS_DIR/monolith-report.json" | python3 -c "
import json, sys
try:
    data = json.load(sys.stdin)
    suites = data.get('suites', [])
    passed = sum(1 for s in suites for t in s.get('specs', []) if t.get('ok'))
    failed = sum(1 for s in suites for t in s.get('specs', []) if not t.get('ok'))
    print(f'  Passed: {passed}, Failed: {failed}')
except:
    print('  Could not parse results')
" 2>/dev/null || echo "  Could not parse results"

echo ""
echo "Microservices results:"
cat "$RESULTS_DIR/microservices-report.json" | python3 -c "
import json, sys
try:
    data = json.load(sys.stdin)
    suites = data.get('suites', [])
    passed = sum(1 for s in suites for t in s.get('specs', []) if t.get('ok'))
    failed = sum(1 for s in suites for t in s.get('specs', []) if not t.get('ok'))
    print(f'  Passed: {passed}, Failed: {failed}')
except:
    print('  Could not parse results')
" 2>/dev/null || echo "  Could not parse results"

echo ""
echo "=== Comparison complete ==="
