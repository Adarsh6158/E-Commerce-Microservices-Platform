import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { cardGradient } from '../utils/gradients';
export function ProductCard({ product, onClick, onAddToCart, addingToCart }) {
  const imgSrc = product.imageUrl || (product.imageUrls && product.imageUrls[0]);
  const [imgError, setImgError] = useState(false);
  const [isHovered, setIsHovered] = useState(false);
  const showFallback = !imgSrc || imgError;
  return (
    <motion.article
      className="product-card"
      onClick={onClick}
      onHoverStart={() => setIsHovered(true)}
      onHoverEnd={() => setIsHovered(false)}
      whileHover={{ y: -6 }}
      whileTap={{ scale: 0.98 }}
      transition={{ type: 'spring', stiffness: 300, damping: 20 }}
      layout
    >
      <div
        className="product-card__image"
        style={showFallback ? { background: cardGradient(product.id) } : {}}
      >
        {!showFallback ? (
          <motion.img
            src={imgSrc}
            alt={product.name}
            className="product-card__img"
            loading="lazy"
            onError={() => setImgError(true)}
            animate={{ scale: isHovered ? 1.08 : 1 }}
            transition={{ duration: 0.4, ease: [0.16, 1, 0.3, 1] }}
          />
        ) : (
          <span className="product-card__image-icon">
            {product.name?.[0]?.toUpperCase() || '?'}
          </span>
        )}
        {}
        <motion.div
          className="product-card__overlay"
          initial={{ opacity: 0 }}
          animate={{ opacity: isHovered ? 1 : 0 }}
          transition={{ duration: 0.25 }}
        >
          <span className="product-card__overlay-text">View Details</span>
        </motion.div>
      </div>
      <div className="product-card__body">
        <h3 className="product-card__name">{product.name}</h3>
        <p className="product-card__price">${Number(product.basePrice).toFixed(2)}</p>
        <p className="product-card__offer">
          <svg width="12" height="12" viewBox="0 0 16 16" fill="none" style={{ verticalAlign: -1 }}>
            <path d="M2 8l4 4 8-8" stroke="hsl(152, 56%, 46%)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
          {' '}Free delivery
        </p>
      </div>
    </motion.article>
  );
}
