// mongo-seed-catalog.js
const categories = [
  { name: "Electronics", slug: "electronics", description: "Laptops, phones, gadgets and accessories" },
  { name: "Clothing", slug: "clothing", description: "Men's and women's fashion apparel" },
  { name: "Home & Garden", slug: "home-garden", description: "Home furnishings and garden supplies" },
  { name: "Books", slug: "books", description: "Fiction, non-fiction, and academic" },
  { name: "Sports & Outdoors", slug: "sports-outdoors", description: "Fitness gear, camping, and sportswear" },
  { name: "Beauty & Health", slug: "beauty-health", description: "Skincare, makeup, vitamins and wellness" },
  { name: "Toys & Games", slug: "toys-games", description: "Board games, puzzles and kids' toys" },
  { name: "Groceries", slug: "groceries", description: "Daily essentials, snacks and beverages" },
];

const catIds = {};
categories.forEach(c => {
  db.categories.updateOne(
    { slug: c.slug },
    { $set: { name: c.name, description: c.description, active: true, updatedAt: new Date() }, $setOnInsert: { parentId: null, createdAt: new Date() } },
    { upsert: true }
  );
  const doc = db.categories.findOne({ slug: c.slug });
  catIds[c.slug] = doc._id.toString();
});

print(`✓ ${Object.keys(catIds).length} categories upserted.`);

const products = [
  { sku: "ELEC-001", name: "Apple MacBook Pro 14 Inch Space Grey", brand: "Apple", basePrice: NumberDecimal("165999.17"), cat: "electronics", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/laptops/apple-macbook-pro-14-inch-space-grey/thumbnail.webp", desc: "The MacBook Pro 14 Inch in Space Grey is a powerful and sleek laptop, featuring Apple's M1 Pro chip for exceptional performance and a stunning Retina display." },
  { sku: "ELEC-002", name: "Asus Zenbook Pro Dual Screen Laptop", brand: "Asus", basePrice: NumberDecimal("149399.17"), cat: "electronics", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/laptops/asus-zenbook-pro-dual-screen-laptop/thumbnail.webp", desc: "The Asus Zenbook Pro Dual Screen Laptop is a high-performance device with dual screens, providing productivity and versatility for creative professionals." },
  { sku: "ELEC-003", name: "Huawei Matebook X Pro", brand: "Huawei", basePrice: NumberDecimal("116199.17"), cat: "electronics", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/laptops/huawei-matebook-x-pro/thumbnail.webp", desc: "The Huawei Matebook X Pro is a slim and stylish laptop with a high-resolution touchscreen display, offering a premium experience for users on the go." },
  { sku: "ELEC-004", name: "Lenovo Yoga 920", brand: "Lenovo", basePrice: NumberDecimal("91299.17"), cat: "electronics", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/laptops/lenovo-yoga-920/thumbnail.webp", desc: "The Lenovo Yoga 920 is a 2-in-1 convertible laptop with a flexible hinge, allowing you to use it as a laptop or tablet, offering versatility and portability." },
  { sku: "ELEC-005", name: "iPhone 5s", brand: "Apple", basePrice: NumberDecimal("16599.17"), cat: "electronics", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/smartphones/iphone-5s/thumbnail.webp", desc: "The iPhone 5s is a classic smartphone known for its compact design and advanced features during its release. While it's an older model, it still provides a reliable user experience." },
  { sku: "ELEC-006", name: "iPhone 6", brand: "Apple", basePrice: NumberDecimal("24899.17"), cat: "electronics", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/smartphones/iphone-6/thumbnail.webp", desc: "The iPhone 6 is a stylish and capable smartphone with a larger display and improved performance. It introduced new features and design elements, making it a popular choice in its time." },
  { sku: "ELEC-007", name: "iPhone 13 Pro", brand: "Apple", basePrice: NumberDecimal("91299.17"), cat: "electronics", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/smartphones/iphone-13-pro/thumbnail.webp", desc: "The iPhone 13 Pro is a cutting-edge smartphone with a powerful camera system, high-performance chip, and stunning display. It offers advanced features for users who demand top-notch technology." },
  { sku: "ELEC-008", name: "iPhone X", brand: "Apple", basePrice: NumberDecimal("74699.17"), cat: "electronics", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/smartphones/iphone-x/thumbnail.webp", desc: "The iPhone X is a flagship smartphone featuring a bezel-less OLED display, facial recognition technology (Face ID), and impressive performance. It represents a milestone in iPhone design and innovation." },
  { sku: "CLOT-001", name: "Blue & Black Check Shirt", brand: "Fashion Trends", basePrice: NumberDecimal("2489.17"), cat: "clothing", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/mens-shirts/blue-&-black-check-shirt/thumbnail.webp", desc: "The Blue & Black Check Shirt is a stylish and comfortable men's shirt featuring a classic check pattern. Made from high-quality fabric, it's suitable for both casual and semi-formal occasions." },
  { sku: "CLOT-002", name: "Gigabyte Aorus Men Tshirt", brand: "Gigabyte", basePrice: NumberDecimal("2074.17"), cat: "clothing", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/mens-shirts/gigabyte-aorus-men-tshirt/thumbnail.webp", desc: "The Gigabyte Aorus Men Tshirt is a cool and casual shirt for gaming enthusiasts. With the Aorus logo and sleek design, it's perfect for expressing your gaming style." },
  { sku: "CLOT-003", name: "Man Plaid Shirt", brand: "Classic Wear", basePrice: NumberDecimal("2904.17"), cat: "clothing", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/mens-shirts/man-plaid-shirt/thumbnail.webp", desc: "The Man Plaid Shirt is a timeless and versatile men's shirt with a classic plaid pattern. Its comfortable fit and casual style make it a wardrobe essential for various occasions." },
  { sku: "CLOT-004", name: "Man Short Sleeve Shirt", brand: "Casual Comfort", basePrice: NumberDecimal("1659.17"), cat: "clothing", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/mens-shirts/man-short-sleeve-shirt/thumbnail.webp", desc: "The Man Short Sleeve Shirt is a breezy and stylish option for warm days. With a comfortable fit and short sleeves, it's perfect for a laid-back yet polished look." },
  { sku: "CLOT-005", name: "Black Women's Gown", brand: "Generic", basePrice: NumberDecimal("10789.17"), cat: "clothing", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/womens-dresses/black-women's-gown/thumbnail.webp", desc: "The Black Women's Gown is an elegant and timeless evening gown. With a sleek black design, it's perfect for formal events and special occasions, exuding sophistication and style." },
  { sku: "CLOT-006", name: "Corset Leather With Skirt", brand: "Generic", basePrice: NumberDecimal("7469.17"), cat: "clothing", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/womens-dresses/corset-leather-with-skirt/thumbnail.webp", desc: "The Corset Leather With Skirt is a bold and edgy ensemble that combines a stylish corset with a matching skirt. Ideal for fashion-forward individuals, it makes a statement at any event." },
  { sku: "CLOT-007", name: "Corset With Black Skirt", brand: "Generic", basePrice: NumberDecimal("6639.17"), cat: "clothing", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/womens-dresses/corset-with-black-skirt/thumbnail.webp", desc: "The Corset With Black Skirt is a chic and versatile outfit that pairs a fashionable corset with a classic black skirt. It offers a trendy and coordinated look for various occasions." },
  { sku: "CLOT-008", name: "Dress Pea", brand: "Generic", basePrice: NumberDecimal("4149.17"), cat: "clothing", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/womens-dresses/dress-pea/thumbnail.webp", desc: "The Dress Pea is a stylish and comfortable dress with a pea pattern. Perfect for casual outings, it adds a playful and fun element to your wardrobe, making it a great choice for day-to-day wear." },
  { sku: "HOME-001", name: "Annibale Colombo Bed", brand: "Annibale Colombo", basePrice: NumberDecimal("157699.17"), cat: "home-garden", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/furniture/annibale-colombo-bed/thumbnail.webp", desc: "The Annibale Colombo Bed is a luxurious and elegant bed frame, crafted with high-quality materials for a comfortable and stylish bedroom." },
  { sku: "HOME-002", name: "Annibale Colombo Sofa", brand: "Annibale Colombo", basePrice: NumberDecimal("207499.17"), cat: "home-garden", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/furniture/annibale-colombo-sofa/thumbnail.webp", desc: "The Annibale Colombo Sofa is a sophisticated and comfortable seating option, featuring exquisite design and premium upholstery for your living room." },
  { sku: "HOME-003", name: "Bedside Table African Cherry", brand: "Furniture Co.", basePrice: NumberDecimal("24899.17"), cat: "home-garden", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/furniture/bedside-table-african-cherry/thumbnail.webp", desc: "The Bedside Table in African Cherry is a stylish and functional addition to your bedroom, providing convenient storage space and a touch of elegance." },
  { sku: "HOME-004", name: "Knoll Saarinen Executive Conference Chair", brand: "Knoll", basePrice: NumberDecimal("41499.17"), cat: "home-garden", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/furniture/knoll-saarinen-executive-conference-chair/thumbnail.webp", desc: "The Knoll Saarinen Executive Conference Chair is a modern and ergonomic chair, perfect for your office or conference room with its timeless design." },
  { sku: "HOME-005", name: "Decoration Swing", brand: "Generic", basePrice: NumberDecimal("4979.17"), cat: "home-garden", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/home-decoration/decoration-swing/thumbnail.webp", desc: "The Decoration Swing is a charming addition to your home decor. Crafted with intricate details, it adds a touch of elegance and whimsy to any room." },
  { sku: "HOME-006", name: "Family Tree Photo Frame", brand: "Generic", basePrice: NumberDecimal("2489.17"), cat: "home-garden", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/home-decoration/family-tree-photo-frame/thumbnail.webp", desc: "The Family Tree Photo Frame is a sentimental and stylish way to display your cherished family memories. With multiple photo slots, it tells the story of your loved ones." },
  { sku: "HOME-007", name: "House Showpiece Plant", brand: "Generic", basePrice: NumberDecimal("3319.17"), cat: "home-garden", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/home-decoration/house-showpiece-plant/thumbnail.webp", desc: "The House Showpiece Plant is an artificial plant that brings a touch of nature to your home without the need for maintenance. It adds greenery and style to any space." },
  { sku: "HOME-008", name: "Plant Pot", brand: "Generic", basePrice: NumberDecimal("1244.17"), cat: "home-garden", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/home-decoration/plant-pot/thumbnail.webp", desc: "The Plant Pot is a stylish container for your favorite plants. With a sleek design, it complements your indoor or outdoor garden, adding a modern touch to your plant display." },
  { sku: "SPOR-001", name: "American Football", brand: "Generic", basePrice: NumberDecimal("1659.17"), cat: "sports-outdoors", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/sports-accessories/american-football/thumbnail.webp", desc: "The American Football is a classic ball used in American football games. It is designed for throwing and catching, making it an essential piece of equipment for the sport." },
  { sku: "SPOR-002", name: "Baseball Ball", brand: "Generic", basePrice: NumberDecimal("746.17"), cat: "sports-outdoors", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/sports-accessories/baseball-ball/thumbnail.webp", desc: "The Baseball Ball is a standard baseball used in baseball games. It features a durable leather cover and is designed for pitching, hitting, and fielding in the game of baseball." },
  { sku: "SPOR-003", name: "Baseball Glove", brand: "Generic", basePrice: NumberDecimal("2074.17"), cat: "sports-outdoors", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/sports-accessories/baseball-glove/thumbnail.webp", desc: "The Baseball Glove is a protective glove worn by baseball players. It is designed to catch and field the baseball, providing players with comfort and control during the game." },
  { sku: "SPOR-004", name: "Basketball", brand: "Generic", basePrice: NumberDecimal("1244.17"), cat: "sports-outdoors", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/sports-accessories/basketball/thumbnail.webp", desc: "The Basketball is a standard ball used in basketball games. It is designed for dribbling, shooting, and passing in the game of basketball, suitable for both indoor and outdoor play." },
  { sku: "BEAU-001", name: "Essence Mascara Lash Princess", brand: "Essence", basePrice: NumberDecimal("829.17"), cat: "beauty-health", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/beauty/essence-mascara-lash-princess/thumbnail.webp", desc: "The Essence Mascara Lash Princess is a popular mascara known for its volumizing and lengthening effects. Achieve dramatic lashes with this long-lasting and cruelty-free formula." },
  { sku: "BEAU-002", name: "Eyeshadow Palette with Mirror", brand: "Glamour Beauty", basePrice: NumberDecimal("1659.17"), cat: "beauty-health", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/beauty/eyeshadow-palette-with-mirror/thumbnail.webp", desc: "The Eyeshadow Palette with Mirror offers a versatile range of eyeshadow shades for creating stunning eye looks. With a built-in mirror, it's convenient for on-the-go makeup application." },
  { sku: "BEAU-003", name: "Powder Canister", brand: "Velvet Touch", basePrice: NumberDecimal("1244.17"), cat: "beauty-health", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/beauty/powder-canister/thumbnail.webp", desc: "The Powder Canister is a finely milled setting powder designed to set makeup and control shine. With a lightweight and translucent formula, it provides a smooth and matte finish." },
  { sku: "BEAU-004", name: "Red Lipstick", brand: "Chic Cosmetics", basePrice: NumberDecimal("1078.17"), cat: "beauty-health", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/beauty/red-lipstick/thumbnail.webp", desc: "The Red Lipstick is a classic and bold choice for adding a pop of color to your lips. With a creamy and pigmented formula, it provides a vibrant and long-lasting finish." },
  { sku: "BEAU-005", name: "Attitude Super Leaves Hand Soap", brand: "Attitude", basePrice: NumberDecimal("746.17"), cat: "beauty-health", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/skin-care/attitude-super-leaves-hand-soap/thumbnail.webp", desc: "Attitude Super Leaves Hand Soap is a natural and nourishing hand soap enriched with the goodness of super leaves. It cleanses and moisturizes your hands, leaving them feeling fresh and soft." },
  { sku: "BEAU-006", name: "Olay Ultra Moisture Shea Butter Body Wash", brand: "Olay", basePrice: NumberDecimal("1078.17"), cat: "beauty-health", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/skin-care/olay-ultra-moisture-shea-butter-body-wash/thumbnail.webp", desc: "Olay Ultra Moisture Shea Butter Body Wash is a luxurious body wash that hydrates and nourishes your skin with the moisturizing power of shea butter. Enjoy a rich lather and silky-smooth skin." },
  { sku: "BEAU-007", name: "Vaseline Men Body and Face Lotion", brand: "Vaseline", basePrice: NumberDecimal("829.17"), cat: "beauty-health", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/skin-care/vaseline-men-body-and-face-lotion/thumbnail.webp", desc: "Vaseline Men Body and Face Lotion is a specially formulated lotion designed to provide long-lasting moisture to men's skin. It absorbs quickly and helps keep the skin hydrated and healthy." },
  { sku: "GROC-001", name: "Apple", brand: "Generic", basePrice: NumberDecimal("165.17"), cat: "groceries", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/groceries/apple/thumbnail.webp", desc: "Fresh and crisp apples, perfect for snacking or incorporating into various recipes." },
  { sku: "GROC-002", name: "Beef Steak", brand: "Generic", basePrice: NumberDecimal("1078.17"), cat: "groceries", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/groceries/beef-steak/thumbnail.webp", desc: "High-quality beef steak, great for grilling or cooking to your preferred level of doneness." },
  { sku: "GROC-003", name: "Cat Food", brand: "Generic", basePrice: NumberDecimal("746.17"), cat: "groceries", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/groceries/cat-food/thumbnail.webp", desc: "Nutritious cat food formulated to meet the dietary needs of your feline friend." },
  { sku: "GROC-004", name: "Chicken Meat", brand: "Generic", basePrice: NumberDecimal("829.17"), cat: "groceries", weight: 0.5, image: "https://cdn.dummyjson.com/product-images/groceries/chicken-meat/thumbnail.webp", desc: "Fresh and tender chicken meat, suitable for various culinary preparations." },
  { sku: "BOOK-001", name: "Atomic Habits by James Clear", brand: "Penguin", basePrice: NumberDecimal("1410.17"), cat: "books", weight: 0.5, image: "https://covers.openlibrary.org/b/isbn/9780593489392-L.jpg", desc: "Build good habits, break bad ones–tiny changes, remarkable results." },
  { sku: "BOOK-002", name: "The Pragmatic Programmer", brand: "Addison-Wesley", basePrice: NumberDecimal("4149.17"), cat: "books", weight: 0.5, image: "https://covers.openlibrary.org/b/isbn/9780201616224-L.jpg", desc: "Classic software development guide." },
  { sku: "BOOK-003", name: "Dune by Frank Herbert", brand: "Ace Books", basePrice: NumberDecimal("912.17"), cat: "books", weight: 0.5, image: "https://covers.openlibrary.org/b/isbn/9780441172719-L.jpg", desc: "Epic science-fiction saga." },
  { sku: "BOOK-004", name: "Clean Code", brand: "Prentice Hall", basePrice: NumberDecimal("3319.17"), cat: "books", weight: 0.5, image: "https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg", desc: "Handbook of agile software craftsmanship." },
  { sku: "BOOK-005", name: "Designing Data-Intensive Applications", brand: "O'Reilly", basePrice: NumberDecimal("3734.17"), cat: "books", weight: 0.5, image: "https://covers.openlibrary.org/b/isbn/9781449373320-L.jpg", desc: "Deep dive into data systems." },
  { sku: "BOOK-006", name: "Sapiens by Yuval Noah Harari", brand: "Harper", basePrice: NumberDecimal("1576.17"), cat: "books", weight: 0.5, image: "https://covers.openlibrary.org/b/isbn/9780062316097-L.jpg", desc: "Brief history of humankind." },
  { sku: "TOYS-001", name: "LEGO Star Wars Millennium Falcon", brand: "LEGO", basePrice: NumberDecimal("13279.17"), cat: "toys-games", weight: 0.5, image: "https://images.unsplash.com/photo-1585366119957-80f30f55cf64?w=400&q=80", desc: "Detailed LEGO model of the Millennium Falcon." },
  { sku: "TOYS-002", name: "Monopoly Classic Board Game", brand: "Hasbro", basePrice: NumberDecimal("1659.17"), cat: "toys-games", weight: 0.5, image: "https://images.unsplash.com/photo-1610890716171-60a6fc434b97?w=400&q=80", desc: "The fast-dealing property trading board game." },
  { sku: "TOYS-003", name: "Hot Wheels 20-Car Pack", brand: "Hot Wheels", basePrice: NumberDecimal("2074.17"), cat: "toys-games", weight: 0.5, image: "https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?w=400&q=80", desc: "Awesome 20-car pack of 1:64 scale vehicles." },
  { sku: "TOYS-004", name: "Rubik's Cube 3x3", brand: "Rubik's", basePrice: NumberDecimal("829.17"), cat: "toys-games", weight: 0.5, image: "https://images.unsplash.com/photo-1591994843349-f4129b61d365?w=400&q=80", desc: "The classic color-matching puzzle." },
  { sku: "TOYS-005", name: "Nerf N-Strike Elite Disruptor", brand: "Nerf", basePrice: NumberDecimal("1244.17"), cat: "toys-games", weight: 0.5, image: "https://images.unsplash.com/photo-1528652422030-f14d86b5155f?w=400&q=80", desc: "Quick-draw blaster with 6-dart rotating drum." },
  { sku: "TOYS-006", name: "Barbie Dreamhouse", brand: "Barbie", basePrice: NumberDecimal("16599.17"), cat: "toys-games", weight: 0.5, image: "https://images.unsplash.com/photo-1590855217887-8dcb4fb0289a?w=400&q=80", desc: "Three-story dollhouse with pool and elevator." },
];

let inserted = 0;
products.forEach(p => {
  const catId = catIds[p.cat];
  db.products.updateOne(
    { sku: p.sku },
    {
      $set: {
        name: p.name,
        description: p.desc,
        categoryId: catId,
        brand: p.brand,
        basePrice: p.basePrice,
        image: p.image,
        thumbnail: p.image,
        galleryImages: [p.image],
        altText: p.name,
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

print(`✓ ${inserted} products upserted.`);
print("✓ catalog_db seeding complete.");
