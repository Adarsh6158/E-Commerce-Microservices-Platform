// ===========================================================
// mongo-seed-catalog.js – Seeds catalog_db with categories,
// products, and reviews.  Idempotent (uses upserts).
// Run against: catalog_db
// ===========================================================

// — Categories (8) ————————————————————————
const categories = [
  { name: "Electronics",    slug: "electronics",     description: "Laptops, phones, gadgets and accessories" },
  { name: "Clothing",       slug: "clothing",         description: "Men's and women's fashion apparel" },
  { name: "Home & Kitchen", slug: "home-kitchen",     description: "Furniture, decor and kitchen essentials" },
  { name: "Books",          slug: "books",            description: "Fiction, non-fiction, and academic" },
  { name: "Sports & Outdoors", slug: "sports-outdoors", description: "Fitness gear, camping, and sportswear" },
  { name: "Beauty & Health", slug: "beauty-health",   description: "Skincare, makeup, vitamins and wellness" },
  { name: "Toys & Games",   slug: "toys-games",       description: "Board games, puzzles and kids' toys" },
  { name: "Groceries",      slug: "groceries",        description: "Daily essentials, snacks and beverages" },
];

const catIds = {};
categories.forEach(c => {
  db.categories.updateOne(
    { slug: c.slug },
    { $set: { ...c, active: true, createdAt: new Date() }, $setOnInsert: { parentId: null } },
    { upsert: true }
  );
  const doc = db.categories.findOne({ slug: c.slug });
  catIds[c.slug] = doc._id.toString();
});

print(`[seed] ${Object.keys(catIds).length} categories upserted.`);

// — Products (50) ————————————————————————
const products = [
  // ———— Electronics (8) ————
  { sku: "ELEC-001", name: "MacBook Pro 16\" M3",           brand: "Apple",    basePrice: NumberDecimal("2499.00"), cat: "electronics", weight: 2.1,  desc: "Powerful laptop with M3 chip, 16\" Liquid Retina XDR display." },
  { sku: "ELEC-002", name: "iPhone 15 Pro Max",              brand: "Apple",    basePrice: NumberDecimal("1199.00"), cat: "electronics", weight: 0.22, desc: "Titanium design, A17 Pro chip, 48MP camera system." },
  { sku: "ELEC-003", name: "Samsung Galaxy S24 Ultra",       brand: "Samsung",  basePrice: NumberDecimal("1099.99"), cat: "electronics", weight: 0.23, desc: "AI-powered phone with S Pen, 200MP camera." },
  { sku: "ELEC-004", name: "Sony WH-1000XM5 Headphones",    brand: "Sony",     basePrice: NumberDecimal("349.99"),  cat: "electronics", weight: 0.25, desc: "Industry-leading noise cancelling wireless headphones." },
  { sku: "ELEC-005", name: "Dell UltraSharp 27\" 4K Monitor", brand: "Dell",   basePrice: NumberDecimal("629.00"),  cat: "electronics", weight: 6.5,  desc: "27-inch 4K UHD USB-C hub monitor for professionals." },
  { sku: "ELEC-006", name: "Logitech MX Master 3S Mouse",   brand: "Logitech", basePrice: NumberDecimal("99.99"),   cat: "electronics", weight: 0.14, desc: "Ergonomic wireless mouse with MagSpeed scroll wheel." },
  { sku: "ELEC-007", name: "iPad Air M2 11\"",               brand: "Apple",    basePrice: NumberDecimal("599.00"),  cat: "electronics", weight: 0.46, desc: "Thin, light, powerful tablet with M2 chip." },
  { sku: "ELEC-008", name: "Bose SoundLink Flex Speaker",   brand: "Bose",     basePrice: NumberDecimal("149.00"),  cat: "electronics", weight: 0.59, desc: "Portable Bluetooth speaker with deep bass." },

  // ———— Clothing (7) ————
  { sku: "CLTH-001", name: "Nike Dri-FIT Running T-Shirt",        brand: "Nike",      basePrice: NumberDecimal("35.00"),  cat: "clothing", weight: 0.15, desc: "Lightweight moisture-wicking running tee." },
  { sku: "CLTH-002", name: "Levi's 501 Original Jeans",           brand: "Levi's",    basePrice: NumberDecimal("69.50"),  cat: "clothing", weight: 0.8,  desc: "Classic straight-leg jeans with button fly." },
  { sku: "CLTH-003", name: "Adidas Ultraboost 23 Shoes",          brand: "Adidas",    basePrice: NumberDecimal("190.00"), cat: "clothing", weight: 0.65, desc: "Responsive Boost midsole with Primeknit upper." },
  { sku: "CLTH-004", name: "Patagonia Better Sweater",            brand: "Patagonia", basePrice: NumberDecimal("139.00"), cat: "clothing", weight: 0.5,  desc: "Warm fleece jacket made from recycled polyester." },
  { sku: "CLTH-005", name: "Uniqlo Heattech Thermal Top",         brand: "Uniqlo",    basePrice: NumberDecimal("19.90"),  cat: "clothing", weight: 0.2,  desc: "Bio-warming thermal innerwear for cold days." },
  { sku: "CLTH-006", name: "Columbia Waterproof Rain Jacket",     brand: "Columbia",  basePrice: NumberDecimal("89.99"),  cat: "clothing", weight: 0.4,  desc: "Packable rain jacket with Omni-Tech waterproofing." },
  { sku: "CLTH-007", name: "Hanes ComfortSoft Cotton Socks 6-Pack", brand: "Hanes",  basePrice: NumberDecimal("12.99"),  cat: "clothing", weight: 0.25, desc: "Soft cotton blend everyday socks." },

  // ———— Home & Kitchen (7) ————
  { sku: "HOME-001", name: "Instant Pot Duo 7-in-1",              brand: "Instant Pot", basePrice: NumberDecimal("89.95"),  cat: "home-kitchen", weight: 5.5,  desc: "Pressure cooker, slow cooker, rice cooker and more." },
  { sku: "HOME-002", name: "Dyson V15 Detect Vacuum",             brand: "Dyson",       basePrice: NumberDecimal("749.99"), cat: "home-kitchen", weight: 3.1,  desc: "Laser-guided cordless vacuum with HEPA filtration." },
  { sku: "HOME-003", name: "KitchenAid Stand Mixer 5qt",          brand: "KitchenAid",  basePrice: NumberDecimal("379.99"), cat: "home-kitchen", weight: 11.5, desc: "Tilt-head stand mixer with 10 speeds." },
  { sku: "HOME-004", name: "Philips Hue Smart Bulb 4-Pack",       brand: "Philips",     basePrice: NumberDecimal("49.99"),  cat: "home-kitchen", weight: 0.6,  desc: "Color-changing smart LED bulbs with app control." },
  { sku: "HOME-005", name: "Nespresso Vertuo Next Coffee Maker",  brand: "Nespresso",   basePrice: NumberDecimal("159.00"), cat: "home-kitchen", weight: 4.0,  desc: "Single-serve capsule coffee machine with crema." },
  { sku: "HOME-006", name: "Cuisinart 14-Cup Food Processor",     brand: "Cuisinart",   basePrice: NumberDecimal("199.95"), cat: "home-kitchen", weight: 7.0,  desc: "Large capacity food processor with multiple blades." },
  { sku: "HOME-007", name: "Lodge Cast Iron Skillet 12\"",        brand: "Lodge",       basePrice: NumberDecimal("39.90"),  cat: "home-kitchen", weight: 3.6,  desc: "Pre-seasoned cast iron for stovetop and oven use." },

  // ———— Books (6) ————
  { sku: "BOOK-001", name: "Atomic Habits by James Clear",                   brand: "Penguin",        basePrice: NumberDecimal("16.99"), cat: "books", weight: 0.35, desc: "Build good habits, break bad ones–tiny changes, remarkable results." },
  { sku: "BOOK-002", name: "The Pragmatic Programmer (20th Anniversary)",    brand: "Addison-Wesley", basePrice: NumberDecimal("49.99"), cat: "books", weight: 0.65, desc: "Classic software development guide–from journeyman to master." },
  { sku: "BOOK-003", name: "Dune by Frank Herbert",                          brand: "Ace Books",      basePrice: NumberDecimal("10.99"), cat: "books", weight: 0.4,  desc: "Epic science-fiction saga of power, betrayal and survival." },
  { sku: "BOOK-004", name: "Designing Data-Intensive Applications",          brand: "O'Reilly",       basePrice: NumberDecimal("44.99"), cat: "books", weight: 0.7,  desc: "Deep dive into the architecture of modern data systems." },
  { sku: "BOOK-005", name: "Sapiens by Yuval Noah Harari",                   brand: "Harper",         basePrice: NumberDecimal("18.99"), cat: "books", weight: 0.45, desc: "Brief history of humankind from the Stone Age to Silicon Valley." },
  { sku: "BOOK-006", name: "Clean Code by Robert C. Martin",                 brand: "Prentice Hall",  basePrice: NumberDecimal("39.99"), cat: "books", weight: 0.55, desc: "Handbook of agile software craftsmanship." },

  // ———— Sports & Outdoors (6) ————
  { sku: "SPRT-001", name: "Yeti Rambler 26oz Water Bottle",       brand: "Yeti",     basePrice: NumberDecimal("35.00"),  cat: "sports-outdoors", weight: 0.5, desc: "Durable stainless steel insulated water bottle." },
  { sku: "SPRT-002", name: "Fitbit Charge 6 Fitness Tracker",      brand: "Fitbit",   basePrice: NumberDecimal("159.95"), cat: "sports-outdoors", weight: 0.04, desc: "Heart rate, sleep tracking, built-in GPS." },
  { sku: "SPRT-003", name: "Coleman 4-Person Sundome Tent",        brand: "Coleman",  basePrice: NumberDecimal("79.99"),  cat: "sports-outdoors", weight: 4.1, desc: "Easy-setup dome tent with WeatherTec system." },
  { sku: "SPRT-004", name: "TRX All-in-One Suspension Trainer",    brand: "TRX",      basePrice: NumberDecimal("149.95"), cat: "sports-outdoors", weight: 0.8, desc: "Full body workout system for any fitness level." },
  { sku: "SPRT-005", name: "Manduka PRO Yoga Mat 6mm",             brand: "Manduka",  basePrice: NumberDecimal("120.00"), cat: "sports-outdoors", weight: 3.5, desc: "Dense cushioning, lifetime guarantee yoga mat." },
  { sku: "SPRT-006", name: "Osprey Atmos AG 65 Backpack",          brand: "Osprey",   basePrice: NumberDecimal("270.00"), cat: "sports-outdoors", weight: 2.2, desc: "Anti-gravity suspension hiking backpack." },

  // ———— Beauty & Health (6) ————
  { sku: "BEAU-001", name: "CeraVe Moisturizing Cream 16oz",           brand: "CeraVe",      basePrice: NumberDecimal("18.99"),  cat: "beauty-health", weight: 0.55, desc: "Rich moisturizer with ceramides and hyaluronic acid." },
  { sku: "BEAU-002", name: "Dyson Airwrap Multi-Styler",               brand: "Dyson",       basePrice: NumberDecimal("599.99"), cat: "beauty-health", weight: 0.66, desc: "Curl, wave, smooth and dry with no extreme heat." },
  { sku: "BEAU-003", name: "Oral-B iO Series 9 Electric Toothbrush",  brand: "Oral-B",      basePrice: NumberDecimal("299.99"), cat: "beauty-health", weight: 0.3,  desc: "AI-powered brushing with interactive display." },
  { sku: "BEAU-004", name: "The Ordinary Niacinamide 10% Serum",      brand: "The Ordinary", basePrice: NumberDecimal("6.50"),   cat: "beauty-health", weight: 0.08, desc: "High-strength vitamin and mineral blemish formula." },
  { sku: "BEAU-005", name: "Neutrogena Hydro Boost Water Gel",        brand: "Neutrogena",  basePrice: NumberDecimal("19.97"),  cat: "beauty-health", weight: 0.2,  desc: "Oil-free gel moisturizer for supple skin." },
  { sku: "BEAU-006", name: "Vitamin D3 5000 IU Softgels 360ct",      brand: "NatureWise",  basePrice: NumberDecimal("14.99"),  cat: "beauty-health", weight: 0.3,  desc: "Immune support and bone health supplement." },

  // ———— Toys & Games (5) ————
  { sku: "TOYS-001", name: "LEGO Technic Porsche 911 GT3 RS",    brand: "LEGO",        basePrice: NumberDecimal("169.99"), cat: "toys-games", weight: 2.8,  desc: "2,704-piece build of the iconic sportscar." },
  { sku: "TOYS-002", name: "Settlers of Catan Board Game",       brand: "Catan Studio", basePrice: NumberDecimal("34.99"),  cat: "toys-games", weight: 1.2,  desc: "Award-winning strategy game for 3-4 players." },
  { sku: "TOYS-003", name: "Nintendo Switch OLED Console",       brand: "Nintendo",    basePrice: NumberDecimal("349.99"), cat: "toys-games", weight: 0.42, desc: "Vibrant 7-inch OLED screen, versatile play modes." },
  { sku: "TOYS-004", name: "Rubik's Cube 3x3 Speed Cube",        brand: "Rubik's",     basePrice: NumberDecimal("11.99"),  cat: "toys-games", weight: 0.1,  desc: "Classic puzzle with smooth turning mechanism." },
  { sku: "TOYS-005", name: "Monopoly Classic Board Game",        brand: "Hasbro",      basePrice: NumberDecimal("19.99"),  cat: "toys-games", weight: 1.0,  desc: "The classic fast-dealing property trading game." },

  // ———— Groceries (5) ————
  { sku: "GROC-001", name: "Kirkland Organic Extra Virgin Olive Oil 2L", brand: "Kirkland",      basePrice: NumberDecimal("16.99"), cat: "groceries", weight: 2.1, desc: "Cold-pressed organic EVOO from Tuscany." },
  { sku: "GROC-002", name: "Barilla Spaghetti No. 5 – 4 Pack",          brand: "Barilla",        basePrice: NumberDecimal("7.49"),  cat: "groceries", weight: 2.0, desc: "Al-dente Italian pasta in 4x500g boxes." },
  { sku: "GROC-003", name: "Green Mountain Coffee K-Cups 72ct",          brand: "Keurig",         basePrice: NumberDecimal("36.99"), cat: "groceries", weight: 1.1, desc: "Medium roast single-serve coffee pods." },
  { sku: "GROC-004", name: "KIND Bars Variety Pack 24ct",                brand: "KIND",           basePrice: NumberDecimal("24.99"), cat: "groceries", weight: 1.0, desc: "Gluten-free nut bars in 6 flavors." },
  { sku: "GROC-005", name: "San Pellegrino Sparkling Water 24-Pack",     brand: "San Pellegrino", basePrice: NumberDecimal("19.99"), cat: "groceries", weight: 8.5, desc: "Natural Italian sparkling mineral water." },
];

let inserted = 0;
products.forEach(p => {
  const catId = catIds[p.cat];
  db.products.updateOne(
    { sku: p.sku },
    { $set: {
        name: p.name,
        description: p.desc,
        categoryId: catId,
        brand: p.brand,
        basePrice: p.basePrice,
        imageUrl: "",
        imageUrls: [],
        active: true,
        weight: p.weight,
        attributes: {},
        updatedAt: new Date()
      },
      $setOnInsert: { createdAt: new Date() }
    },
    { upsert: true }
  );
  inserted++;
});

print(`[seed] ${inserted} products upserted.`);

// — Sample Reviews (2 per first 10 products) ————————————
const reviewers = [
  { userName: "TechGuru42",    userId: "user-001" },
  { userName: "ShopaholicMom", userId: "user-002" },
  { userName: "BargainHunter", userId: "user-003" },
  { userName: "OutdoorKing",   userId: "user-004" },
];

const prodDocs = db.products.find().sort({ sku: 1 }).limit(10).toArray();
let revCount = 0;
prodDocs.forEach((prod, i) => {
  for (let r = 0; r < 2; r++) {
    const reviewer = reviewers[(i + r) % reviewers.length];
    const rating = 3 + ((i + r) % 3); // 3, 4, or 5
    db.reviews.updateOne(
      { productId: prod._id.toString(), userId: reviewer.userId },
      { $set: {
          userName: reviewer.userName,
          rating: rating,
          title: `Great ${rating === 5 ? 'product' : rating === 4 ? 'value' : 'purchase'}!`,
          comment: `Solid quality for the price. Would ${rating >= 4 ? 'definitely' : 'probably'} buy again.`,
          verified: true,
          createdAt: new Date()
        }
      },
      { upsert: true }
    );
    revCount++;
  }
});

print(`[seed] ${revCount} reviews upserted.`);
print("[seed] catalog_db seeding complete.");
