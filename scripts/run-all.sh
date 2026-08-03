#!/usr/bin/env bash
# Bring up the whole composed system (8 Spring Boot apps) for E2E testing.
#
#   ./scripts/run-all.sh [--no-build] [--keep-db]
#
# Each service runs from its own module directory so its SQLite DB file stays
# next to the module. Logs go to build/run-logs/<service>.log.
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-11-openjdk-amd64}"
export JAVA_HOME
JAVA="$JAVA_HOME/bin/java"

BUILD=1
KEEP_DB=0
for arg in "$@"; do
  case "$arg" in
    --no-build) BUILD=0 ;;
    --keep-db) KEEP_DB=1 ;;
  esac
done

LOG_DIR="$ROOT/build/run-logs"
PID_DIR="$ROOT/build/run-pids"
mkdir -p "$LOG_DIR" "$PID_DIR"

# service_name:module_dir:jar_glob:port
SERVICES=(
  "user-service:user-service:user-service/build/libs/user-service*.jar:8081"
  "monolith:.:build/libs/realworld*.jar:8082"
  "tag-service:tag-service:tag-service/build/libs/tag-service*.jar:8083"
  "favorite-service:favorite-service:favorite-service/build/libs/favorite-service*.jar:8084"
  "profile-service:profile-service:profile-service/build/libs/profile-service*.jar:8085"
  "comment-service:comment-service:comment-service/build/libs/comment-service*.jar:8086"
  "article-service:article-service:article-service/build/libs/article-service*.jar:8087"
  "gateway:gateway:gateway/build/libs/gateway*.jar:8080"
)

DB_FILES=(
  "user-service/user.db" "tag-service/tag.db" "favorite-service/favorite.db"
  "profile-service/profile.db" "comment-service/comment.db"
  "article-service/article.db" "dev.db"
)

stop_all() {
  for f in "$PID_DIR"/*.pid; do
    [ -e "$f" ] || continue
    pid="$(cat "$f")"
    kill "$pid" 2>/dev/null
    rm -f "$f"
  done
  # belt and braces: anything still bound to our ports
  for port in 8080 8081 8082 8083 8084 8085 8086 8087; do
    fuser -k "${port}/tcp" 2>/dev/null
  done
  sleep 2
}

wait_for_port() {
  local port="$1" name="$2" tries=180
  for ((i = 0; i < tries; i++)); do
    if (echo >"/dev/tcp/127.0.0.1/$port") 2>/dev/null; then
      echo "  $name is listening on :$port"
      return 0
    fi
    sleep 1
  done
  echo "  ERROR: $name did not come up on :$port; tail of log:" >&2
  tail -30 "$LOG_DIR/$name.log" >&2
  return 1
}

echo "== stopping any previous run"
stop_all

if [ "$BUILD" = "1" ]; then
  echo "== building (bootJar for every module)"
  ./gradlew bootJar :user-service:bootJar :tag-service:bootJar :favorite-service:bootJar \
    :profile-service:bootJar :comment-service:bootJar :article-service:bootJar :gateway:bootJar \
    -q || { echo "build failed" >&2; exit 1; }
fi

if [ "$KEEP_DB" = "0" ]; then
  echo "== removing SQLite DBs so Flyway re-seeds each service"
  for db in "${DB_FILES[@]}"; do rm -f "$ROOT/$db"; done
fi

for entry in "${SERVICES[@]}"; do
  IFS=":" read -r name dir jarglob port <<<"$entry"
  jar="$(ls $ROOT/$jarglob 2>/dev/null | grep -v plain | head -1)"
  if [ -z "$jar" ]; then
    echo "ERROR: no jar found for $name ($jarglob)" >&2
    exit 1
  fi
  echo "== starting $name on :$port"
  (cd "$ROOT/$dir" && nohup "$JAVA" -jar "$jar" >"$LOG_DIR/$name.log" 2>&1 & echo $! >"$PID_DIR/$name.pid")
  wait_for_port "$port" "$name" || exit 1
done

echo
echo "== all services up. Sanity check via the gateway:"
curl -s -o /dev/null -w "  GET /tags      -> %{http_code}\n" http://localhost:8080/tags
curl -s -o /dev/null -w "  GET /articles  -> %{http_code}\n" http://localhost:8080/articles
TOKEN=$(curl -s -X POST http://localhost:8080/users/login \
  -H 'Content-Type: application/json' \
  -d '{"user":{"email":"john@example.com","password":"password123"}}' |
  sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
if [ -n "$TOKEN" ]; then
  curl -s -o /dev/null -w "  GET /user      -> %{http_code} (authenticated)\n" \
    -H "Authorization: Token $TOKEN" http://localhost:8080/user
else
  echo "  ERROR: login did not return a token" >&2
fi
echo
echo "Logs: $LOG_DIR   Stop everything: ./scripts/stop-all.sh"
