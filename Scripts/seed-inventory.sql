-- ===========================================================
-- seed-inventory.sql – Creates tables if needed.
-- The actual data INSERT is done by seed-data.sh dynamically
-- (it reads product IDs from MongoDB and INSERTs here).
-- ===========================================================

-- Ensure tables exist (mirrors Flyway V1 + V2 migrations)
CREATE TABLE IF NOT EXISTS inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL UNIQUE,
    warehouse_id VARCHAR(100) NOT NULL DEFAULT 'DEFAULT',
    available_quantity INTEGER NOT NULL DEFAULT 0 CHECK (available_quantity >= 0),
    reserved_quantity INTEGER NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_inventory_product_id ON inventory(product_id);
CREATE INDEX IF NOT EXISTS idx_inventory_sku ON inventory(sku);

CREATE TABLE IF NOT EXISTS reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    sku VARCHAR(50),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(order_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_reservations_order_id ON reservations(order_id);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);
