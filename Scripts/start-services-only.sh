#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v mvn &>/dev/null; then
  echo "[ERROR] Maven not found."
  exit 1
fi

MVN="mvn"
LOG_DIR="${ROOT_DIR}/logs"
mkdir -p "$LOG_DIR"
PIDS=""

cleanup() {
  echo ""
  echo "Stopping services..."
  for pid in $PIDS; do
    kill "$pid" 2>/dev/null || true
  done
  wait 2>/dev/null || true
}
trap cleanup EXIT INT TERM

# -------- SERVICE CONFIG --------

service_pom() {
  case "$1" in
    gateway)      echo "Gateway/pom.xml" ;;
    auth)         echo "Backend/auth-service/pom.xml" ;;
    catalog)      echo "Backend/catalog-service/pom.xml" ;;
    search)       echo "Backend/search-service/pom.xml" ;;
    cart)         echo "Backend/cart-service/pom.xml" ;;
    pricing)      echo "Backend/pricing-service/pom.xml" ;;
    inventory)    echo "Backend/inventory-service/pom.xml" ;;
    order)        echo "Backend/order-service/pom.xml" ;;
    payment)      echo "Backend/payment-service/pom.xml" ;;
    notification) echo "Backend/notification-service/pom.xml" ;;
    analytics)    echo "Backend/analytics-service/pom.xml" ;;
    *) echo "" ;;
  esac
}

service_port() {
  case "$1" in
    gateway) echo 8080 ;;
    auth) echo 8081 ;;
    catalog) echo 8082 ;;
    search) echo 8083 ;;
    cart) echo 8084 ;;
    pricing) echo 8085 ;;
    inventory) echo 8086 ;;
    order) echo 8087 ;;
    payment) echo 8088 ;;
    notification) echo 8089 ;;
    analytics) echo 8090 ;;
    *) echo "" ;;
  esac
}

# -------- UTIL FUNCTIONS --------

kill_port() {
  local port=$1
  if command -v lsof &>/dev/null; then
    lsof -ti :"$port" | xargs kill -9 2>/dev/null || true
  fi
}

check_port() {
  local port=$1
  local retries=15

  for i in $(seq 1 $retries); do
    if lsof -i :"$port" >/dev/null 2>&1; then
      echo "[UP] Port $port is running ✅"
      return 0
    fi
    sleep 1
  done

  echo "[DOWN] Port $port is NOT running ❌"
  return 1
}

check_health() {
  local port=$1
  local retries=10

  for i in $(seq 1 $retries); do
    local status=$(curl -s "http://localhost:$port/actuator/health" | grep -o '"status":"UP"' || true)

    if [[ "$status" == *"UP"* ]]; then
      echo "[HEALTH] Service on port $port is UP ✅"
      return 0
    fi
    sleep 2
  done

  echo "[HEALTH] Service on port $port is NOT healthy ❌"
  return 1
}

start_service() {
  local name=$1
  local pom=$(service_pom "$name")

  if [ -z "$pom" ]; then
    echo "[ERROR] Unknown service: $name"
    exit 1
  fi

  local port=$(service_port "$name")

  if [ -n "$port" ]; then
    kill_port "$port"
  fi

  local log_file="$LOG_DIR/${name}.log"
  echo "[START] $name (port $port)"
  echo "       logs -> $log_file"

  $MVN spring-boot:run -f "$pom" -q > "$log_file" 2>&1 &
  local pid=$!
  PIDS="$PIDS $pid"

  echo "[PID] $name -> $pid"

  # Check port
  if [ -n "$port" ]; then
    check_port "$port"
    check_health "$port"
  fi

  echo ""
}

# -------- MAIN --------

MODE="${1:-all}"
ALL="gateway auth catalog search cart pricing inventory order payment notification analytics"

if [ "$MODE" = "all" ]; then
  SERVICES="$ALL"
else
  SERVICES="gateway $MODE"
fi

echo "Starting services: $SERVICES"
echo "Logs directory: $LOG_DIR"
echo ""

# Start gateway first
start_service gateway
sleep 2

# Start remaining services
for svc in $SERVICES; do
  if [ "$svc" = "gateway" ]; then continue; fi
  start_service "$svc"
  sleep 1
done

echo "All services attempted to start 🚀"
echo "Gateway: http://localhost:8080"
echo ""

wait