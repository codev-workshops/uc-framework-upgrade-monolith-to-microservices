#!/bin/bash
#
# api-compare.sh - Compare API responses between monolith and microservices
#
# Usage: ./api-compare.sh [monolith_port] [microservices_port]
#

set -e

MONOLITH_PORT=${1:-8080}
MICROSERVICES_PORT=${2:-9080}
MONOLITH_URL="http://localhost:$MONOLITH_PORT"
MICROSERVICES_URL="http://localhost:$MICROSERVICES_PORT"
TMPDIR=$(mktemp -d)
PASS=0
FAIL=0
SKIP=0

cleanup() {
    rm -rf "$TMPDIR"
}
trap cleanup EXIT

compare_endpoint() {
    local description="$1"
    local method="$2"
    local endpoint="$3"
    local auth_header="$4"

    echo -n "  Testing: $description ... "

    local curl_opts="-s -w \n%{http_code}"
    if [ -n "$auth_header" ]; then
        curl_opts="$curl_opts -H \"Authorization: $auth_header\""
    fi

    # Fetch from monolith
    if [ -n "$auth_header" ]; then
        MONOLITH_RESPONSE=$(curl -s -w "\n%{http_code}" -H "Authorization: $auth_header" "$MONOLITH_URL$endpoint" 2>/dev/null) || true
    else
        MONOLITH_RESPONSE=$(curl -s -w "\n%{http_code}" "$MONOLITH_URL$endpoint" 2>/dev/null) || true
    fi

    # Fetch from microservices
    if [ -n "$auth_header" ]; then
        MICRO_RESPONSE=$(curl -s -w "\n%{http_code}" -H "Authorization: $auth_header" "$MICROSERVICES_URL$endpoint" 2>/dev/null) || true
    else
        MICRO_RESPONSE=$(curl -s -w "\n%{http_code}" "$MICROSERVICES_URL$endpoint" 2>/dev/null) || true
    fi

    if [ -z "$MONOLITH_RESPONSE" ] || [ -z "$MICRO_RESPONSE" ]; then
        echo "SKIP (one or both services unavailable)"
        SKIP=$((SKIP + 1))
        return
    fi

    # Extract status codes
    MONOLITH_STATUS=$(echo "$MONOLITH_RESPONSE" | tail -1)
    MICRO_STATUS=$(echo "$MICRO_RESPONSE" | tail -1)

    # Extract bodies
    MONOLITH_BODY=$(echo "$MONOLITH_RESPONSE" | sed '$d')
    MICRO_BODY=$(echo "$MICRO_RESPONSE" | sed '$d')

    # Compare status codes
    if [ "$MONOLITH_STATUS" != "$MICRO_STATUS" ]; then
        echo "FAIL (status: monolith=$MONOLITH_STATUS, microservices=$MICRO_STATUS)"
        FAIL=$((FAIL + 1))
        return
    fi

    # Compare response bodies (normalize JSON for comparison)
    echo "$MONOLITH_BODY" | python3 -m json.tool --sort-keys > "$TMPDIR/monolith.json" 2>/dev/null || echo "$MONOLITH_BODY" > "$TMPDIR/monolith.json"
    echo "$MICRO_BODY" | python3 -m json.tool --sort-keys > "$TMPDIR/micro.json" 2>/dev/null || echo "$MICRO_BODY" > "$TMPDIR/micro.json"

    if diff -q "$TMPDIR/monolith.json" "$TMPDIR/micro.json" > /dev/null 2>&1; then
        echo "PASS (status: $MONOLITH_STATUS)"
        PASS=$((PASS + 1))
    else
        echo "DIFF (status: $MONOLITH_STATUS - response bodies differ)"
        FAIL=$((FAIL + 1))
        echo "    --- Monolith ---"
        head -5 "$TMPDIR/monolith.json" | sed 's/^/    /'
        echo "    --- Microservices ---"
        head -5 "$TMPDIR/micro.json" | sed 's/^/    /'
    fi
}

echo "=========================================="
echo "  API Response Comparison"
echo "=========================================="
echo "  Monolith:       $MONOLITH_URL"
echo "  Microservices:  $MICROSERVICES_URL"
echo "=========================================="
echo ""

# Get auth token from monolith
echo "  Authenticating..."
AUTH_RESPONSE=$(curl -s -X POST "$MONOLITH_URL/users/login" \
    -H "Content-Type: application/json" \
    -d '{"user":{"email":"john@example.com","password":"password123"}}' 2>/dev/null) || true

TOKEN=$(echo "$AUTH_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['user']['token'])" 2>/dev/null) || true

if [ -z "$TOKEN" ]; then
    echo "  WARNING: Could not authenticate. Authenticated endpoints will be skipped."
fi
echo ""

# Public endpoints
echo "--- Public Endpoints ---"
compare_endpoint "GET /tags" "GET" "/tags"
compare_endpoint "GET /articles" "GET" "/articles"
compare_endpoint "GET /articles?limit=2" "GET" "/articles?limit=2&offset=0"
compare_endpoint "GET /articles?tag=java" "GET" "/articles?tag=java"
compare_endpoint "GET /articles?author=johndoe" "GET" "/articles?author=johndoe"
compare_endpoint "GET /articles/:slug" "GET" "/articles/getting-started-with-spring-boot"
compare_endpoint "GET /articles/:slug/comments" "GET" "/articles/getting-started-with-spring-boot/comments"
compare_endpoint "GET /profiles/:username" "GET" "/profiles/johndoe"
echo ""

# Authenticated endpoints
if [ -n "$TOKEN" ]; then
    echo "--- Authenticated Endpoints ---"
    compare_endpoint "GET /user" "GET" "/user" "Token $TOKEN"
    compare_endpoint "GET /articles/feed" "GET" "/articles/feed" "Token $TOKEN"
    echo ""
fi

# Summary
echo "=========================================="
echo "  Results Summary"
echo "=========================================="
echo "  Passed:  $PASS"
echo "  Failed:  $FAIL"
echo "  Skipped: $SKIP"
TOTAL=$((PASS + FAIL + SKIP))
echo "  Total:   $TOTAL"
echo "=========================================="

if [ $FAIL -gt 0 ]; then
    exit 1
fi
