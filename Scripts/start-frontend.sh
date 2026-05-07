#!/usr/bin/env bash
set -e

# ============================================================
# start-frontend.sh - Start Shell + MFEs
# Run from: ecommerce-platform/ (project root)
#
# Usage:
#   ./scripts/start-frontend.sh                # build + start all MFEs + shell
#   ./scripts/start-frontend.sh product        # build + start product-mfe + shell only
#   ./scripts/start-frontend.sh cart,order     # build + start cart-mfe + order-mfe + shell
#
# NOTE: Compatible with Bash 3.2+ (macOS default) - no declare -A.
# ============================================================

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
PIDS=""

cleanup() {
  echo ""
  echo "Shutting down frontend processes..."
  for pid in $PIDS; do kill "$pid" 2>/dev/null || true; done
  wait 2>/dev/null
  echo "All frontend processes stopped."
}
trap cleanup EXIT INT TERM

# — MFE lookup functions (bash 3.2 compatible) —
mfe_dir() {
  case "$1" in
    product) echo "product-mfe" ;;
    search) echo "search-mfe" ;;
    cart) echo "cart-mfe" ;;
    order) echo "order-mfe" ;;
    admin) echo "admin-mfe" ;;
    *) echo "" ;;
  esac
}

mfe_port() {
  case "$1" in
    product) echo 3001 ;;
    search) echo 3002 ;;
    cart) echo 3003 ;;
    order) echo 3004 ;;
    admin) echo 3005 ;;
    *) echo "" ;;
  esac
}

ALL_MFES="product search cart order admin"

# — Parse arguments —
MODE="${1:-all}"

if [ "$MODE" = "all" ]; then
  MFES_TO_START="$ALL_MFES"
else
  MFES_TO_START=""
  OLD_IFS="$IFS"; IFS=','
  for t in $MODE; do
    IFS="$OLD_IFS"
    t=$(echo "$t" | tr -d ' ')
    dir=$(mfe_dir "$t")
    if [ -z "$dir" ]; then
      echo "[error] Unknown MFE: $t"
      echo "Valid names: product search cart order admin"
      exit 1
    fi
    MFES_TO_START="$MFES_TO_START $t"
  done
  IFS="$OLD_IFS"
fi

echo "============================================"
echo "  E-Commerce Frontend Launcher"
echo "  Starting:${MFES_TO_START}"
echo "============================================"
echo ""

# — Step 1: Install deps if needed —
for mfe in $MFES_TO_START; do
  dir=$(mfe_dir "$mfe")
  if [ ! -d "$FRONTEND_DIR/$dir/node_modules" ]; then
    echo "[install] $dir"
    (cd "$FRONTEND_DIR/$dir" && npm install --silent)
  fi
done

if [ ! -d "$FRONTEND_DIR/shell/node_modules" ]; then
  echo "[install] shell"
  (cd "$FRONTEND_DIR/shell" && npm install --silent)
fi

# — Step 2: Build MFEs —
for mfe in $MFES_TO_START; do
  dir=$(mfe_dir "$mfe")
  echo "[build] $dir"
  (cd "$FRONTEND_DIR/$dir" && npx vite build --logLevel error)
done

echo "[build] shell"
(cd "$FRONTEND_DIR/shell" && npx vite build --logLevel error)
echo "[build] All selected MFEs + shell built."
echo ""

# — Helper: kill process on a port —
kill_port() {
  local port="$1"
  local pids
  pids=$(lsof -ti :"$port" 2>/dev/null || true)
  if [ -n "$pids" ]; then
    echo "[cleanup] Killing existing process on port $port (PIDs: $pids)"
    echo "$pids" | xargs kill -9 2>/dev/null || true
    sleep 1
  fi
}

# — Step 3: Start MFE preview servers —
for mfe in $MFES_TO_START; do
  dir=$(mfe_dir "$mfe")
  port=$(mfe_port "$mfe")
  kill_port "$port"
  echo "[start] $dir -> :$port"
  (cd "$FRONTEND_DIR/$dir" && npx vite preview --port "$port" --strictPort --host) &
  PIDS="$PIDS $!"
done

sleep 2

# — Step 4: Start Shell in preview mode —
kill_port 3000
echo "[start] shell -> :3000 (preview mode)"
(cd "$FRONTEND_DIR/shell" && npx vite preview --port 3000 --strictPort --host) &
PIDS="$PIDS $!"

echo ""
echo "============================================"
echo "  Frontend running!"
echo "  Shell:   http://localhost:3000"
echo "  MFEs:    ${MFES_TO_START}"
echo "============================================"
echo "Press Ctrl+C to stop all."
echo ""

wait