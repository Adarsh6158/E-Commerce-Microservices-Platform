// ===========================================================
// mongo-seed-pricing.js – Seeds pricing_db with pricing rules.
// Idempotent (upserts by name).
// Run against: pricing_db
// ===========================================================

// —————— Helper: get category IDs from catalog_db ——————
// Since pricing rules reference categoryId/productId from catalog,
// and we don't have cross-db joins in Mongo, we use category slugs
// as a naming convention and store actual ObjectId strings.

const rulesData = [
  // — Category-wide discounts —
  {
    name: "Electronics Summer Sale",
    ruleType: "PERCENTAGE_DISCOUNT",
    priority: 10,
    categorySlug: "electronics",
    conditions: { minOrderAmount: 100 },
    discount: { percentage: 10 },
    tiers: [],
    validFrom: new Date("2025-01-01"),
    validUntil: new Date("2027-12-31"),
  },
  {
    name: "Clothing Clearance 15% Off",
    ruleType: "PERCENTAGE_DISCOUNT",
    priority: 8,
    categorySlug: "clothing",
    conditions: {},
    discount: { percentage: 15 },
    tiers: [],
    validFrom: new Date("2025-01-01"),
    validUntil: new Date("2027-12-31"),
  },
  {
    name: "Books Flat $5 Off",
    ruleType: "FIXED_DISCOUNT",
    priority: 5,
    categorySlug: "books",
    conditions: { minOrderAmount: 20 },
    discount: { amount: 5 },
    tiers: [],
    validFrom: new Date("2025-01-01"),
    validUntil: new Date("2027-12-31"),
  },
  {
    name: "Beauty Buy 1 Get 1 Free",
    ruleType: "BOGO",
    priority: 12,
    categorySlug: "beauty-health",
    conditions: { minQuantity: 2 },
    discount: { buyQuantity: 1, getQuantity: 1 },
    tiers: [],
    validFrom: new Date("2025-06-01"),
    validUntil: new Date("2027-12-31"),
  },
  {
    name: "Groceries Tiered Pricing",
    ruleType: "TIERED",
    priority: 6,
    categorySlug: "groceries",
    conditions: {},
    discount: {},
    tiers: [
      { minQuantity: 1,  maxQuantity: 5,   pricePerUnit: NumberDecimal("0") },
      { minQuantity: 6,  maxQuantity: 12,  pricePerUnit: NumberDecimal("-1.00") },
      { minQuantity: 13, maxQuantity: 100, pricePerUnit: NumberDecimal("-2.00") },
    ],
    validFrom: new Date("2025-01-01"),
    validUntil: new Date("2027-12-31"),
  },
  {
    name: "Sports Weekend Flash Sale",
    ruleType: "TIME_BASED",
    priority: 15,
    categorySlug: "sports-outdoors",
    conditions: { dayOfWeek: ["SATURDAY", "SUNDAY"] },
    discount: { percentage: 20 },
    tiers: [],
    validFrom: new Date("2025-01-01"),
    validUntil: new Date("2027-12-31"),
  },
  {
    name: "New Year Home Appliance Deal",
    ruleType: "PERCENTAGE_DISCOUNT",
    priority: 7,
    categorySlug: "home-kitchen",
    conditions: { minOrderAmount: 50 },
    discount: { percentage: 12 },
    tiers: [],
    validFrom: new Date("2025-01-01"),
    validUntil: new Date("2027-12-31"),
  },
  {
    name: "Toys Holiday Special",
    ruleType: "FIXED_DISCOUNT",
    priority: 9,
    categorySlug: "toys-games",
    conditions: {},
    discount: { amount: 10 },
    tiers: [],
    validFrom: new Date("2025-11-01"),
    validUntil: new Date("2027-01-31"),
  },

  // — Product-specific rules (use SKU to find productId) —
  {
    name: "MacBook Pro Bundle Discount",
    ruleType: "PERCENTAGE_DISCOUNT",
    priority: 20,
    productSku: "ELEC-001",
    conditions: { minQuantity: 2 },
    discount: { percentage: 5 },
    tiers: [],
    validFrom: new Date("2025-01-01"),
    validUntil: new Date("2027-12-31"),
  },
  {
    name: "Instant Pot Price Drop",
    ruleType: "FIXED_DISCOUNT",
    priority: 18,
    productSku: "HOME-001",
    conditions: {},
    discount: { amount: 15 },
    tiers: [],
    validFrom: new Date("2025-01-01"),
    validUntil: new Date("2027-12-31"),
  },
  {
    name: "LEGO Technic Bulk Tiered",
    ruleType: "TIERED",
    priority: 14,
    productSku: "TOYS-001",
    conditions: {},
    discount: {},
    tiers: [
      { minQuantity: 1, maxQuantity: 1,   pricePerUnit: NumberDecimal("169.99") },
      { minQuantity: 2, maxQuantity: 3,   pricePerUnit: NumberDecimal("159.99") },
      { minQuantity: 4, maxQuantity: 100, pricePerUnit: NumberDecimal("149.99") },
    ],
    validFrom: new Date("2025-01-01"),
    validUntil: new Date("2027-12-31"),
  },
  {
    name: "Niacinamide Serum BOGO",
    ruleType: "BOGO",
    priority: 16,
    productSku: "BEAU-004",
    conditions: { minQuantity: 2 },
    discount: { buyQuantity: 1, getQuantity: 1 },
    tiers: [],
    validFrom: new Date("2025-01-01"),
    validUntil: new Date("2027-12-31"),
  },
];

// — Resolve category / product IDs from catalog_db ————————
// We read from the sibling catalog_db to get real ObjectId strings.
const catalogDb = db.getSiblingDB("catalog_db");
const catMap = {};
catalogDb.categories.find().forEach(c => { catMap[c.slug] = c._id.toString(); });
const skuMap = {};
catalogDb.products.find().forEach(p => { skuMap[p.sku] = p._id.toString(); });

let count = 0;
rulesData.forEach(r => {
  const doc = {
    name: r.name,
    ruleType: r.ruleType,
    priority: r.priority,
    active: true,
    conditions: r.conditions || {},
    discount: r.discount || {},
    tiers: r.tiers || [],
    validFrom: r.validFrom,
    validUntil: r.validUntil,
    updatedAt: new Date(),
  };

  if (r.categorySlug) {
    doc.categoryId = catMap[r.categorySlug] || "";
    doc.productId = null;
  } else if (r.productSku) {
    doc.productId = skuMap[r.productSku] || "";
    doc.categoryId = null;
  }

  db.pricing_rules.updateOne(
    { name: r.name },
    { $set: doc, $setOnInsert: { createdAt: new Date() } },
    { upsert: true }
  );
  count++;
});

print(`[seed] ${count} pricing rules upserted.`);
print("[seed] pricing_db seeding complete.");
