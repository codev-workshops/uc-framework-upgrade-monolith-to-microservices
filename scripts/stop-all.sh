#!/usr/bin/env bash
# Stop everything started by scripts/run-all.sh
set -uo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_DIR="$ROOT/build/run-pids"

for f in "$PID_DIR"/*.pid; do
  [ -e "$f" ] || continue
  pid="$(cat "$f")"
  echo "stopping $(basename "$f" .pid) (pid $pid)"
  kill "$pid" 2>/dev/null
  rm -f "$f"
done

for port in 8080 8081 8082 8083 8084 8085 8086 8087; do
  fuser -k "${port}/tcp" 2>/dev/null
done
echo "done"
