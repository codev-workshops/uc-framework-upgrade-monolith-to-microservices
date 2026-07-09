#!/usr/bin/env bash
# Runs all microservices + the api-gateway locally (no Docker), wiring them over localhost.
# Requires JDK 11 and pre-built jars: `for s in services/*/; do (cd "$s" && ./gradlew bootJar -x test); done`
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RUN="${RUN_DIR:-$ROOT/.local-run}"
mkdir -p "$RUN"

start() {
  local name=$1; shift
  echo "starting $name ..."
  java -jar "$ROOT/services/$name/build/libs/"*.jar "$@" > "$RUN/$name.log" 2>&1 &
  echo $! > "$RUN/$name.pid"
}

start user-auth-service
start favorite-service --services.user-auth.url=http://localhost:8081
start profile-service  --services.user-auth.url=http://localhost:8081
start article-service  --services.user-auth.url=http://localhost:8081 --services.favorite.url=http://localhost:8084 --services.profile.url=http://localhost:8082
start comment-service  --services.article.url=http://localhost:8083 --services.user-auth.url=http://localhost:8081 --services.profile.url=http://localhost:8082
start api-gateway      --services.user-auth.url=http://localhost:8081 --services.profile.url=http://localhost:8082 --services.article.url=http://localhost:8083 --services.favorite.url=http://localhost:8084 --services.comment.url=http://localhost:8085
echo "all started; gateway on http://localhost:8080 (pids in $RUN)"
