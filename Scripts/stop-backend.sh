#!/usr/bin/env bash
# Usage:
#   ./scripts/stop-backend.sh          # kill Java processes only
#   ./scripts/stop-backend.sh --infra  # also run docker compose down

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

# Auto-detect compose tool
if command -v docker &>/dev/null && docker compose version &>/dev/null; then
  COMPOSE="docker compose"
elif command -v podman-compose &>/dev/null; then
  COMPOSE="podman-compose"
else
  COMPOSE=""
fi

echo "======================"
echo " Stopping Backend Services"
echo "======================"

# Kill all spring-boot:run Maven processes
MAVEN_PIDS=$(pgrep -f "spring-boot:run" 2>/dev/null || true)
if [ -n "$MAVEN_PIDS" ]; then
  echo "Stopping Maven/Spring Boot processes..."
  echo "$MAVEN_PIDS" | xargs kill 2>/dev/null || true
  sleep 2
  # Force-kill any survivors
  REMAINING=$(pgrep -f "spring-boot:run" 2>/dev/null || true)
  if [ -n "$REMAINING" ]; then
    echo "$REMAINING" | xargs kill -9 2>/dev/null || true
  fi
  echo "Backend services stopped."
else
  echo "No running backend services found."
fi

if [ "$1" = "--infra" ]; then
  echo ""
  if [ -n "$COMPOSE" ]; then
    echo "Stopping infrastructure ($COMPOSE)…"
    $COMPOSE down
    echo "Infrastructure stopped."
  else
    echo "[error] No compose tool found to stop infrastructure."
  fi
fi

echo "Done."