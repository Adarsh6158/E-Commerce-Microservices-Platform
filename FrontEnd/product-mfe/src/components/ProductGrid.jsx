import React from 'react';
import { motion } from 'framer-motion';
import { ProductCard } from './ProductCard';
const containerVariants = {
  hidden: {},
  show: {
    transition: {
      staggerChildren: 0.04,
      delayChildren: 0.1,
    },
  },
};
const itemVariants = {
  hidden: { opacity: 0, y: 16 },
  show: {
    opacity: 1,
    y: 0,
    transition: { type: 'spring', stiffness: 120, damping: 14 },
  },
};
export function ProductGrid({ products, categoryName, onClickProduct }) {
  if (products.length === 0) {
    return (
      <motion.div
        className="product-grid__empty"
        initial={{ opacity: 0, scale: 0.96 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.3, ease: [0.16, 1, 0.3, 1] }}
      >
        <span className="product-grid__empty-icon">🔍</span>
        <p className="product-grid__empty-text">No products found in this category</p>
      </motion.div>
    );
  }
  return (
    <motion.div
      className="product-grid-section"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.3 }}
    >
      {categoryName && (
        <motion.div
          className="category-heading"
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
        >
          <h2 className="category-heading__title">{categoryName}</h2>
          <p className="category-heading__count">{products.length} product{products.length !== 1 ? 's' : ''}</p>
        </motion.div>
      )}
      <motion.div
        className="product-grid"
        variants={containerVariants}
        initial="hidden"
        animate="show"
      >
        {products.map(p => (
          <motion.div key={p.id} variants={itemVariants}>
            <ProductCard product={p} onClick={() => onClickProduct(p.id)} />
          </motion.div>
        ))}
      </motion.div>
    </motion.div>
  );
}
