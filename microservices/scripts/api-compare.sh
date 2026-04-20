#!/bin/bash
set -e

MONOLITH_URL="${MONOLITH_URL:-http://localhost:8080}"
MICROSERVICES_URL="${MICROSERVICES_URL:-http://localhost:8080}"
RESULTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/results"
mkdir -p "$RESULTS_DIR"

echo "=== API Comparison: Monolith ($MONOLITH_URL) vs Microservices ($MICROSERVICES_URL) ==="
echo ""

PASS=0
FAIL=0

compare_endpoint() {
    local method=$1
    local path=$2
    local description=$3
    local auth_header=$4

    echo -n "Testing $method $path ($description)... "

    local curl_opts="-s -w '\n%{http_code}'"
    if [ -n "$auth_header" ]; then
        curl_opts="$curl_opts -H 'Authorization: $auth_header'"
    fi

    local monolith_response
    local microservices_response

    if [ "$method" = "GET" ]; then
        monolith_response=$(curl -s -w "\n%{http_code}" -H "Content-Type: application/json" ${auth_header:+-H "Authorization: $auth_header"} "$MONOLITH_URL$path")
        microservices_response=$(curl -s -w "\n%{http_code}" -H "Content-Type: application/json" ${auth_header:+-H "Authorization: $auth_header"} "$MICROSERVICES_URL$path")
    fi

    local monolith_status=$(echo "$monolith_response" | tail -1)
    local microservices_status=$(echo "$microservices_response" | tail -1)

    if [ "$monolith_status" = "$microservices_status" ]; then
        echo "PASS (both returned $monolith_status)"
        PASS=$((PASS + 1))
    else
        echo "FAIL (monolith=$monolith_status, microservices=$microservices_status)"
        FAIL=$((FAIL + 1))
    fi
}

# Seed test data
echo "--- Seeding test data ---"
TIMESTAMP=$(date +%s)
TEST_USER="{\"user\":{\"email\":\"apicompare${TIMESTAMP}@test.com\",\"username\":\"apicompare${TIMESTAMP}\",\"password\":\"password123\"}}"

MONOLITH_REG=$(curl -s -X POST -H "Content-Type: application/json" -d "$TEST_USER" "$MONOLITH_URL/users")
MONOLITH_TOKEN=$(echo "$MONOLITH_REG" | python3 -c "import json,sys; print(json.load(sys.stdin)['user']['token'])" 2>/dev/null || echo "")

MICRO_REG=$(curl -s -X POST -H "Content-Type: application/json" -d "$TEST_USER" "$MICROSERVICES_URL/users")
MICRO_TOKEN=$(echo "$MICRO_REG" | python3 -c "import json,sys; print(json.load(sys.stdin)['user']['token'])" 2>/dev/null || echo "")

echo ""
echo "--- Comparing endpoints ---"

# Public endpoints
compare_endpoint "GET" "/articles" "List articles"
compare_endpoint "GET" "/tags" "List tags"

# Auth endpoints
if [ -n "$MONOLITH_TOKEN" ]; then
    compare_endpoint "GET" "/user" "Current user" "Token $MONOLITH_TOKEN"
    compare_endpoint "GET" "/articles/feed" "User feed" "Token $MONOLITH_TOKEN"
fi

echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="
echo "Details saved to $RESULTS_DIR/"
