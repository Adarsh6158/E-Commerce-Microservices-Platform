#!/usr/bin/env bash
set -euo pipefail

# ===========================================================
# start-backend.sh – Start infrastructure + backend services
# Run from: ecommerce-platform/  (project root)
#
# Usage:
#   ./scripts/start-backend.sh                  # start infra + all services
#   ./scripts/start-backend.sh infra            # start infra only
#   ./scripts/start-backend.sh catalog          # start infra + gateway + catalog-service
#   ./scripts/start-backend.sh catalog,cart     # start infra + gateway + catalog + cart
#
# Compatible with Bash 3.2+ (macOS default)
# ===========================================================

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

# Verify Maven is installed
if ! command -v mvn &>/dev/null; then
  echo "[ERROR] Maven not found. Please install Maven."
  exit 1
fi

MVN="mvn"
SETTINGS=""
PIDS=""
LOG_DIR="${ROOT_DIR}/logs"
mkdir -p "$LOG_DIR"

# Auto-detect compose tool
if command -v docker &>/dev/null && docker compose version &>/dev/null 2>&1; then
  COMPOSE="docker compose"
elif command -v podman-compose &>/dev/null; then
  COMPOSE="podman-compose"
else
  echo "[ERROR] Neither 'docker compose' nor 'podman-compose' found."
  exit 1
fi

cleanup() {
  echo ""
  echo "Shutting down backend processes…"
  for pid in $PIDS; do 
    if kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
    fi
  done
  wait 2>/dev/null || true
  echo "All backend processes stopped."
  echo "(Infrastructure is still running. Use: $COMPOSE -f Infra/docker-compose.yml down)"
}
trap cleanup EXIT INT TERM

# Service lookup function (bash 3.2 compatible)
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
    *)            echo "" ;;
  esac
}

ALL_SERVICES="gateway auth catalog search cart pricing inventory order payment notification analytics"

# Service port lookup (bash 3.2 compatible)
service_port() {
  case "$1" in
    gateway)      echo 8080 ;;
    auth)         echo 8081 ;;
    catalog)      echo 8082 ;;
    search)       echo 8083 ;;
    cart)         echo 8084 ;;
    pricing)      echo 8085 ;;
    inventory)    echo 8086 ;;
    order)        echo 8087 ;;
    payment)      echo 8088 ;;
    notification) echo 8089 ;;
    analytics)    echo 8090 ;;
    *)            echo "" ;;
  esac
}

# Check if a port is in use (cross-platform compatible)
port_in_use() {
  local port=$1
  # Try lsof first, fall back to netstat if not available
  if command -v lsof &>/dev/null; then
    lsof -ti :"$port" 2>/dev/null || true
  elif command -v netstat &>/dev/null; then
    netstat -tuln 2>/dev/null | grep ":$port " || true
  else
    # Last resort: try to connect
    timeout 1 bash -c "< /dev/null > /dev/tcp/localhost/$port" 2>/dev/null && echo "in_use" || true
  fi
}

# Wait for a service to be healthy (via health endpoint)
wait_for_service() {
  local service=$1
  local port=$2
  local max_retries=60
  local retries=0
  
  echo "[health] Waiting for $service (port $port)..."
  while [ $retries -lt $max_retries ]; do
    if curl -sf http://localhost:"$port"/actuator/health &>/dev/null 2>&1; then
      echo "[health] $service is healthy ✓"
      return 0
    fi
    retries=$((retries + 1))
    sleep 1
  done
  
  echo "[WARN] $service did not become healthy after ${max_retries}s (check logs)"
  return 1
}

# Parse arguments
MODE="${1:-all}"

start_infra() {
  echo "=========================================="
  echo "  Starting Infrastructure ($COMPOSE)…"
  echo "=========================================="
  $COMPOSE -f Infra/docker-compose.yml up -d 2>&1 | tee "$LOG_DIR/infra.log"

  echo ""
  echo "Waiting for infrastructure services to be healthy…"
  local infra_retries=0
  local max_retries=120
  
  while [ $infra_retries -lt $max_retries ]; do
    # Check critical services
    local kafka_ok=0
    local mongo_ok=0
    local postgres_ok=0
    local redis_ok=0
    
    $COMPOSE -f Infra/docker-compose.yml ps | grep -q "kafka.*healthy" && kafka_ok=1 || true
    $COMPOSE -f Infra/docker-compose.yml ps | grep -q "mongodb.*healthy" && mongo_ok=1 || true
    $COMPOSE -f Infra/docker-compose.yml ps | grep -q "postgres.*healthy" && postgres_ok=1 || true
    $COMPOSE -f Infra/docker-compose.yml ps | grep -q "redis.*healthy" && redis_ok=1 || true
    
    if [ $kafka_ok -eq 1 ] && [ $mongo_ok -eq 1 ] && [ $postgres_ok -eq 1 ] && [ $redis_ok -eq 1 ]; then
      echo "[health] All infrastructure services are healthy ✓"
      return 0
    fi
    
    infra_retries=$((infra_retries + 1))
    if [ $((infra_retries % 10)) -eq 0 ]; then
      echo "[health] Still waiting... (${infra_retries}s elapsed)"
    fi
    sleep 1
  done
  
  echo "[WARN] Infrastructure services not all healthy after ${max_retries}s"
  echo "[INFO] Docker compose status:"
  $COMPOSE -f Infra/docker-compose.yml ps
  return 0  # Continue anyway, services might still work
}

start_service() {
  local name=$1
  local pom
  pom=$(service_pom "$name")
  if [ -z "$pom" ]; then
    echo "[ERROR] Unknown service: $name"
    echo "Valid names: $ALL_SERVICES"
    exit 1
  fi

  local port
  port=$(service_port "$name")
  
  if [ -n "$port" ]; then
    local existing
    existing=$(port_in_use "$port" || true)
    if [ -n "$existing" ] && [ "$existing" != "" ]; then
      echo "[WARN] Port $port already in use, killing existing process..."
      if command -v lsof &>/dev/null; then
        echo "$existing" | xargs kill -9 2>/dev/null || true
      fi
      sleep 2
    fi
  fi

  local log_file="$LOG_DIR/${name}-service.log"
  echo "[start] $name (logging to $log_file)"
  
  $MVN $SETTINGS spring-boot:run -f "$pom" -q > "$log_file" 2>&1 &
  local pid=$!
  PIDS="$PIDS $pid"
  
  # Give service time to start
  sleep 2
  
  # Verify the service actually started
  if ! kill -0 "$pid" 2>/dev/null; then
    echo "[ERROR] $name failed to start. Check $log_file for details."
    tail -20 "$log_file"
    return 1
  fi
  
  # Wait for health check
  if [ -n "$port" ]; then
    wait_for_service "$name" "$port" || echo "[WARN] $name may not be fully ready, continuing anyway..."
  fi
}

echo "=========================================="
echo "  E-Commerce Backend Launcher"
echo "  Mode: $MODE"
echo "  Logs: $LOG_DIR"
echo "=========================================="
echo ""

# Step 1: Always start infrastructure
start_infra

if [ "$MODE" = "infra" ]; then
  echo "Infrastructure-only mode. Exiting."
  trap - EXIT
  exit 0
fi

# Step 2: Start services
if [ "$MODE" = "all" ]; then
  SERVICES_TO_START="$ALL_SERVICES"
else
  # Parse comma-separated list, always include gateway
  SERVICES_TO_START="gateway"
  OLD_IFS="$IFS"; IFS=','
  for t in $MODE; do
    IFS="$OLD_IFS"
    t=$(echo "$t" | tr -d ' ')
    if [ "$t" != "gateway" ]; then
      SERVICES_TO_START="$SERVICES_TO_START $t"
    fi
  done
  IFS="$OLD_IFS"
fi

# Start services in proper order: gateway first, then auth, then the rest
if echo "$SERVICES_TO_START" | grep -qw "gateway"; then
  start_service gateway
  sleep 3
fi
if echo "$SERVICES_TO_START" | grep -qw "auth"; then
  start_service auth
  sleep 2
fi
for svc in $SERVICES_TO_START; do
  if [ "$svc" = "gateway" ] || [ "$svc" = "auth" ]; then continue; fi
  start_service "$svc"
  sleep 1
done

echo ""
echo "=========================================="
echo "  Backend services starting…"
echo "  Gateway:     http://localhost:8080"
echo "  Kafdrop:     http://localhost:9000"
echo "  Log Files:   $LOG_DIR"
echo "=========================================="
echo "Press Ctrl+C to stop all services."
echo ""

wait
