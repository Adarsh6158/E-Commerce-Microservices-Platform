import React, { useEffect, useState, useMemo } from 'react';
import { fetchProducts, fetchProduct, fetchCategories, fetchPrice, fetchReviews, fetchRating, submitReview, addToCart } from './api';
import { useToast } from './hooks/useToast';
import { useRecentlyViewed } from './hooks/useRecentlyViewed';
import { ToastContainer } from './components/ToastContainer';
import { CategoryFilterBar } from './components/CategoryFilterBar';
import { HeroCarousel } from './components/HeroCarousel';
import { ProductGrid } from './components/ProductGrid';
import { ProductDetail } from './components/ProductDetail';
import { CategoryCarousel } from './components/CategoryCarousel';
import { LazyScrollSections } from './components/LazyScrollSections';
import { RecentlyViewed } from './components/RecentlyViewed';
import './App.css';

export default function App({ initialProductId, initialBrand } = {}) {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [activeCategoryId, setActiveCategoryId] = useState(null);
  const [activeBrand, setActiveBrand] = useState(initialBrand || null);
  const [selected, setSelected] = useState(null);
  const [price, setPrice] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [rating, setRating] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [addingToCart, setAddingToCart] = useState(false);
  const { toasts, show: showToast, dismiss: dismissToast } = useToast();
  const { recentlyViewed, addRecentlyViewed } = useRecentlyViewed();
  const [initialOpened, setInitialOpened] = useState(false);

  useEffect(() => {
    (async () => {
      try { setProducts(await fetchProducts()); } catch (e) { setError(e.message); }
      try { setCategories(await fetchCategories()); } catch { setCategories([]); }
      setLoading(false);
    })();
  }, []);

  // Auto-open product detail when navigated with ?id= param
  useEffect(() => {
    if (initialProductId && !loading && !initialOpened) {
      setInitialOpened(true);
      openProduct(initialProductId);
    }
  }, [initialProductId, loading, initialOpened]);

  const categoryMap = useMemo(() => {
    const map = {};
    categories.forEach(c => { map[c.id] = c; });
    return map;
  }, [categories]);

  // Products filtered by active category or brand
  const filteredProducts = useMemo(() => {
    let filtered = products;
    if (activeCategoryId) {
      filtered = filtered.filter(p => p.categoryId === activeCategoryId);
    }
    if (activeBrand) {
      filtered = filtered.filter(p => p.brand === activeBrand);
    }
    return filtered;
  }, [products, activeCategoryId, activeBrand]);

  // Active title for heading display
  const activeTitle = useMemo(() => {
    if (activeBrand) return `${activeBrand} Collection`;
    if (activeCategoryId) return categoryMap[activeCategoryId]?.name || null;
    return null;
  }, [activeBrand, activeCategoryId, categoryMap]);

  // Grouped products for the "For You" view
  const groupedProducts = useMemo(() => {
    const grouped = {};
    products.forEach(p => {
      const catId = p.categoryId || 'uncategorized';
      if (!grouped[catId]) grouped[catId] = [];
      grouped[catId].push(p);
    });
    return grouped;
  }, [products]);

  const categoryOrder = useMemo(() => {
    return categories.length > 0
      ? categories.map(c => c.id).filter(id => groupedProducts[id]?.length > 0)
      : Object.keys(groupedProducts);
  }, [categories, groupedProducts]);

  const openProduct = async (id) => {
    try {
      const prod = await fetchProduct(id);
      setSelected(prod);
      addRecentlyViewed(prod);
      const [pr, rv, rt] = await Promise.allSettled([
        fetchPrice(id, 1, prod.basePrice),
        fetchReviews(id),
        fetchRating(id),
      ]);
      setPrice(pr.status === 'fulfilled' ? pr.value : null);
      setReviews(rv.status === 'fulfilled' ? (rv.value || []) : []);
      setRating(rt.status === 'fulfilled' ? rt.value : null);
    } catch (e) { setError(e.message); }
  };

  const handleAddToCart = async (product) => {
    setAddingToCart(true);
    try {
      await addToCart(product);
      showToast(`${product.name} added to cart`);
      window.dispatchEvent(new CustomEvent('mfe:cart:updated'));
    } catch (e) {
      showToast(e.message || 'Failed to add to cart', 'error');
    } finally {
      setAddingToCart(false);
    }
  };

  const handleSubmitReview = async (productId, reviewData) => {
    try {
      const newReview = await submitReview(productId, reviewData);
      setReviews(prev => [newReview, ...prev]);
      setRating(await fetchRating(productId));
      showToast('Review submitted successfully!');
    } catch (e) {
      showToast(e.message || 'Failed to submit review', 'error');
    }
  };

  const closeDetail = () => {
    setSelected(null);
    setPrice(null);
    setReviews([]);
    setRating(null);
  };

  if (loading) return (
    <div className="products-loading">
      <div className="spinner"></div>
      <p className="products-loading__text">Loading products…</p>
    </div>
  );

  if (error) return (
    <div className="products-error">
      <div className="products-error__icon">⚠️</div>
      <p className="products-error__text">{error}</p>
      <button onClick={() => window.location.reload()} className="products-error__retry">Try again</button>
    </div>
  );

  if (selected) {
    return (
      <div>
        <ToastContainer toasts={toasts} onDismiss={dismissToast} />
        <ProductDetail
          product={selected}
          price={price}
          reviews={reviews}
          rating={rating}
          addingToCart={addingToCart}
          onAddToCart={handleAddToCart}
          onBack={closeDetail}
          onSubmitReview={handleSubmitReview}
        />
      </div>
    );
  }

  return (
    <div>
      <ToastContainer toasts={toasts} onDismiss={dismissToast} />

      {/* 1. Category Filter Bar */}
      <CategoryFilterBar
        categories={categories}
        activeCategoryId={activeCategoryId}
        onSelectCategory={setActiveCategoryId}
      />

      {/* 2. Hero Carousel (only on "For You" / no filter) */}
      {!activeCategoryId && !activeBrand && (
        <HeroCarousel
          categories={categories}
          onSelectCategory={setActiveCategoryId}
        />
      )}

      {products.length === 0 ? (
        <div className="empty-state">
          <p className="empty-state__text">No products yet. Use the Admin panel to add some.</p>
        </div>
      ) : (activeCategoryId || activeBrand) ? (
        /* 3a. Filtered grid when a category or brand is selected */
        <ProductGrid
          products={filteredProducts}
          categoryName={activeTitle}
          onClickProduct={openProduct}
        />
      ) : (
        /* 3b. "For You" view - category carousels */
        <>
          {categoryOrder.map(catId => {
            const cat = categoryMap[catId];
            const items = groupedProducts[catId] || [];
            return (
              <section key={catId} className="deal-section">
                <h2 className="deal-section__title">{cat?.name || 'Other'}</h2>
                <CategoryCarousel products={items} onClickProduct={openProduct} />
              </section>
            );
          })}

          {/* 4. Scroll-based lazy-loaded sections */}
          <LazyScrollSections products={products} onClickProduct={openProduct} />
        </>
      )}

      {/* 5. Recently Viewed (shows only when items exist) */}
      <RecentlyViewed items={recentlyViewed} onClickProduct={openProduct} />
    </div>
  );
}
