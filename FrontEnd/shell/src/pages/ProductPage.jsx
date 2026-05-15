import React, { Suspense, lazy, useEffect, useMemo } from 'react';
import { useLocation } from 'react-router-dom';
import { ErrorBoundary } from '../components/ErrorBoundary';
import { onMfeEvent, MfeEvents, emitMfeEvent } from '../lib/events';
import { useAuth } from '../lib/auth';

const ProductApp = lazy(() => import('productMfe/App'));

function LoadingFallback() {
  return (
    <div className="products-loading">
      <div className="spinner"></div>
      <p className="products-loading__text">Loading products…</p>
    </div>
  );
}

export default function ProductPage() {
  const { user, isAuthenticated } = useAuth();
  const location = useLocation();

  const initialProductId = useMemo(() => {
    const params = new URLSearchParams(location.search);
    return params.get('id') || null;
  }, [location.search]);

  const initialBrand = useMemo(() => {
    const params = new URLSearchParams(location.search);
    return params.get('brand') || null;
  }, [location.search]);

  useEffect(() => {
    emitMfeEvent(MfeEvents.AUTH_CHANGED, { user, isAuthenticated });
  }, [user, isAuthenticated]);

  useEffect(() => {
    return onMfeEvent(MfeEvents.CART_ADD, () => {
      emitMfeEvent(MfeEvents.CART_UPDATED);
    });
  }, []);

  return (
    <ErrorBoundary>
      <Suspense fallback={<LoadingFallback />}>
        <ProductApp 
          initialProductId={initialProductId} 
          initialBrand={initialBrand} 
        />
      </Suspense>
    </ErrorBoundary>
  );
}