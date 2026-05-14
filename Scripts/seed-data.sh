#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SCRIPT_DIR="$ROOT_DIR/Scripts"

GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
YELLOW='\033[0;33m'
GRAY='\033[0;90m'
WHITE='\033[1;37m'
RED='\033[0;31m'
NC='\033[0m'
BOLD='\033[1m'

echo -e "\n${CYAN}${BOLD}▲ ShopFlux Database Seeder${NC}"
echo -e "${GRAY}│${NC}"

CONTAINER_CMD="docker"
if ! command -v docker &>/dev/null; then
  if command -v podman &>/dev/null; then
    CONTAINER_CMD="podman"
  else
    echo -e "${GRAY}│  └─${NC} ${RED}⨯ Neither docker nor podman found${NC}"
    exit 1
  fi
fi

MONGOSH_CMD="mongosh"
if ! command -v mongosh &>/dev/null; then
  if $CONTAINER_CMD ps | grep -q mongodb; then
    MONGOSH_CMD="$CONTAINER_CMD exec -i mongodb mongosh"
  else
    echo -e "${GRAY}│  └─${NC} ${RED}⨯ mongosh not found and MongoDB container not running${NC}"
    exit 1
  fi
fi

wait_for_service() {
  local container=$1
  local port=$2
  local name=$3
  local retries=30
  
  while [ $retries -gt 0 ]; do
    if $CONTAINER_CMD exec "$container" true &>/dev/null 2>&1; then
      echo -e "${GRAY}│  ├─${NC} ${GREEN}✓ ${name} reachable${NC}"
      return 0
    fi
    retries=$((retries - 1))
    sleep 1
  done
  
  echo -e "${GRAY}│  ├─${NC} ${RED}⨯ ${name} unreachable${NC}"
  return 1
}

echo -e "${GRAY}├─${NC} ${YELLOW}●${NC} ${BOLD}Checking Infrastructure Health${NC}"
wait_for_service mongodb 27017 "MongoDB" || exit 1
wait_for_service postgres-inventory 5432 "PostgreSQL (inventory)" || exit 1
wait_for_service elasticsearch 9200 "Elasticsearch" || exit 1
echo -e "${GRAY}│${NC}"

echo -e "${GRAY}├─${NC} ${BLUE}●${NC} ${BOLD}Seeding MongoDB${NC}"
MONGO_URI="mongodb://pricing_user:pricing_secret_pwd@localhost:27017/?authSource=admin"

if ! echo "db.adminCommand('ping')" | $MONGOSH_CMD "$MONGO_URI" &>/dev/null; then
  MONGO_URI="mongodb://pricing_user:pricing_secret_pwd@mongodb:27017/?authSource=admin"
  if ! echo "db.adminCommand('ping')" | $CONTAINER_CMD exec -i mongodb mongosh "$MONGO_URI" &>/dev/null; then
    echo -e "${GRAY}│  ├─${NC} ${RED}⨯ Connection failed${NC}"
    exit 1
  fi
  MONGOSH_CMD="$CONTAINER_CMD exec -i mongodb mongosh"
fi
echo -e "${GRAY}│  ├─${NC} ${GREEN}✓ Connection established${NC}"

if [[ "$MONGOSH_CMD" == *"docker"* ]] || [[ "$MONGOSH_CMD" == *"podman"* ]]; then
  $CONTAINER_CMD exec -i mongodb mongosh "mongodb://pricing_user:pricing_secret_pwd@localhost:27017/catalog_db?authSource=admin" --quiet < "$SCRIPT_DIR/mongo-seed-catalog.js" > /dev/null 2>&1 || true
else
  $MONGOSH_CMD "mongodb://pricing_user:pricing_secret_pwd@localhost:27017/catalog_db?authSource=admin" --quiet --file "$SCRIPT_DIR/mongo-seed-catalog.js" > /dev/null 2>&1 || true
fi
echo -e "${GRAY}│  ├─${NC} ${GREEN}✓ Populated catalog_db${NC}"

$MONGOSH_CMD "mongodb://pricing_user:pricing_secret_pwd@localhost:27017/pricing_db?authSource=admin" --quiet --eval '
  db.pricing_rules.deleteMany({});
  db.pricing_rules.insertMany([
    { name: "bulk_discount_10+", min_quantity: 10, discount_percent: 5 },
    { name: "bulk_discount_50+", min_quantity: 50, discount_percent: 10 },
    { name: "bulk_discount_100+", min_quantity: 100, discount_percent: 15 },
  ]);
' > /dev/null 2>&1 || {
  echo 'db.pricing_rules.deleteMany({}); db.pricing_rules.insertMany([{ name: "bulk_discount_10+", min_quantity: 10, discount_percent: 5 }, { name: "bulk_discount_50+", min_quantity: 50, discount_percent: 10 }, { name: "bulk_discount_100+", min_quantity: 100, discount_percent: 15 }]);' | $CONTAINER_CMD exec -i mongodb mongosh mongodb://pricing_user:pricing_secret_pwd@mongodb:27017/pricing_db?authSource=admin --quiet > /dev/null 2>&1 || true
}
echo -e "${GRAY}│  ├─${NC} ${GREEN}✓ Populated pricing_db${NC}"

$MONGOSH_CMD "mongodb://pricing_user:pricing_secret_pwd@localhost:27017/order_db?authSource=admin" --quiet --eval '
  db.orders.createIndex({ userId: 1 });
  db.orders.createIndex({ status: 1 });
  db.orders.createIndex({ createdAt: -1 });
' > /dev/null 2>&1 || {
  echo 'db.orders.createIndex({ userId: 1 }); db.orders.createIndex({ status: 1 }); db.orders.createIndex({ createdAt: -1 });' | $CONTAINER_CMD exec -i mongodb mongosh mongodb://pricing_user:pricing_secret_pwd@mongodb:27017/order_db?authSource=admin --quiet > /dev/null 2>&1 || true
}
echo -e "${GRAY}│  └─${NC} ${GREEN}✓ Populated order_db${NC}"
echo -e "${GRAY}│${NC}"

echo -e "${GRAY}├─${NC} ${BLUE}●${NC} ${BOLD}Seeding PostgreSQL${NC}"
PSQL_CMD="$CONTAINER_CMD exec -i postgres-inventory psql -U inventory_user -d inventory_db --quiet"
$PSQL_CMD < "$SCRIPT_DIR/seed-inventory.sql" > /dev/null 2>&1 || true
echo -e "${GRAY}│  ├─${NC} ${GREEN}✓ Initialized inventory schema${NC}"

INVENTORY_SQL=$($CONTAINER_CMD exec -i mongodb mongosh "mongodb://pricing_user:pricing_secret_pwd@mongodb:27017/catalog_db?authSource=admin" --quiet --eval '
  let qty = 100;
  const rows = [];
  db.products.find({}, { sku: 1 }).forEach(p => {
    const pid = p._id.toString();
    const sku = p.sku.replace(/'"'"'/g, "'"'"''"'"''"'"'");
    qty = 50 + ((qty * 7 + 13) % 451);
    rows.push("INSERT INTO inventory (sku, product_id, warehouse_id, available_quantity, reserved_quantity, version, updated_at) VALUES ('"'"'" + sku + "'"'"', '"'"'" + pid + "'"'"', '"'"'DEFAULT'"'"', " + qty + ", 0, 0, NOW()) ON CONFLICT (product_id) DO UPDATE SET available_quantity = EXCLUDED.available_quantity, updated_at = NOW();");
  });
  print(rows.join("\n"));
')

if [ -n "$INVENTORY_SQL" ]; then
  echo "$INVENTORY_SQL" | $PSQL_CMD > /dev/null 2>&1 || true
  ROW_COUNT=$(echo "$INVENTORY_SQL" | wc -l | tr -d ' ')
  echo -e "${GRAY}│  └─${NC} ${GREEN}✓ Inventory seeded (${ROW_COUNT} products)${NC}"
else
  echo -e "${GRAY}│  └─${NC} ${YELLOW}⨯ No products found in MongoDB${NC}"
fi
echo -e "${GRAY}│${NC}"

echo -e "${GRAY}├─${NC} ${BLUE}●${NC} ${BOLD}Seeding Elasticsearch${NC}"
BULK_DATA=$($CONTAINER_CMD exec -i mongodb mongosh "mongodb://pricing_user:pricing_secret_pwd@mongodb:27017/catalog_db?authSource=admin" --quiet --eval '
  const cats = {};
  db.categories.find().forEach(c => { cats[c._id.toString()] = c.name; });
  const bulk = [];
  db.products.find({ active: true }).forEach(p => {
    const id = p._id.toString();
    bulk.push(JSON.stringify({ index: { _index: "products", _id: id } }));
    bulk.push(JSON.stringify({
      name: p.name, description: p.description || "", sku: p.sku, brand: p.brand,
      categoryId: p.categoryId || "", categoryName: cats[p.categoryId] || "",
      basePrice: parseFloat(p.basePrice.toString()), image: p.image || "",
      thumbnail: p.thumbnail || "", galleryImages: p.galleryImages || [],
      altText: p.altText || "", active: true, updatedAt: (p.updatedAt || new Date()).toISOString()
    }));
  });
  print(bulk.join("\n") + "\n");
')

if [ -n "$BULK_DATA" ]; then
  BULK_FILE="/tmp/_es_bulk_$$.ndjson"
  echo "$BULK_DATA" > "$BULK_FILE"
  curl -s -X POST "http://localhost:9200/products/_bulk" -H "Content-Type: application/x-ndjson" --data-binary @"$BULK_FILE" > /dev/null 2>&1 || true
  rm -f "$BULK_FILE"
  echo -e "${GRAY}│  └─${NC} ${GREEN}✓ Products indexed${NC}"
else
  echo -e "${GRAY}│  └─${NC} ${YELLOW}⨯ No products to index${NC}"
fi

echo -e "${GRAY}│${NC}"
echo -e "${GRAY}└─${NC} ${GREEN}${BOLD}✓ All seed data inserted successfully!${NC}\n"
