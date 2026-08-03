#!/usr/bin/env bash
# Build and serve the Next.js frontend on :3000 for E2E runs.
#
# A production build is used on purpose: in `next dev`, Fast Refresh remounts
# components (the pages export anonymous functions) and wipes the local state of
# the editor / comment forms mid-test, which makes the E2E suite flaky.
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT/frontend"

LOG_DIR="$ROOT/build/run-logs"
PID_DIR="$ROOT/build/run-pids"
mkdir -p "$LOG_DIR" "$PID_DIR"

if [ -f "$PID_DIR/frontend.pid" ]; then
  kill "$(cat "$PID_DIR/frontend.pid")" 2>/dev/null
  rm -f "$PID_DIR/frontend.pid"
fi
fuser -k 3000/tcp 2>/dev/null
sleep 1

# Next 9 requires Node 14-16.
if [ -s "$HOME/.nvm/nvm.sh" ]; then
  # shellcheck disable=SC1091
  . "$HOME/.nvm/nvm.sh"
  nvm install 16 >/dev/null 2>&1
  nvm use 16 >/dev/null
fi

[ -d node_modules ] || npm install
npm run build || { echo "frontend build failed" >&2; exit 1; }

nohup npm start >"$LOG_DIR/frontend.log" 2>&1 &
echo $! >"$PID_DIR/frontend.pid"

for _ in $(seq 1 120); do
  if (echo >/dev/tcp/127.0.0.1/3000) 2>/dev/null; then
    echo "frontend is listening on :3000"
    exit 0
  fi
  sleep 1
done

echo "ERROR: frontend did not come up on :3000" >&2
tail -30 "$LOG_DIR/frontend.log" >&2
exit 1
