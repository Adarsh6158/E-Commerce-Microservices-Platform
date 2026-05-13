import React from 'react';
import { motion } from 'framer-motion';
const CATEGORY_ICONS = {
  'Electronics':     '🖥️',
  'Clothing':        '👕',
  'Home & Kitchen':  '🏠',
  'Home & Garden':   '🪴',
  'Books':           '📚',
  'Sports & Outdoors': '⚽',
  'Beauty & Health': '💄',
  'Toys & Games':    '🧸',
  'Groceries':       '🛒',
};
const DEFAULT_ICON = '🔥';
export function CategoryFilterBar({ categories, activeCategoryId, onSelectCategory }) {
  return (
    <div className="category-bar">
      <div className="category-bar__track">
        <motion.button
          className={`category-bar__item ${!activeCategoryId ? 'category-bar__item--active' : ''}`}
          onClick={() => onSelectCategory(null)}
          whileHover={{ scale: 1.04 }}
          whileTap={{ scale: 0.96 }}
        >
          {!activeCategoryId && (
            <motion.span
              className="category-bar__active-bg"
              layoutId="category-active"
              transition={{ type: 'spring', stiffness: 400, damping: 30 }}
            />
          )}
          <span className="category-bar__icon">🔥</span>
          <span className="category-bar__label">For You</span>
        </motion.button>
        {categories.map(cat => (
          <motion.button
            key={cat.id}
            className={`category-bar__item ${activeCategoryId === cat.id ? 'category-bar__item--active' : ''}`}
            onClick={() => onSelectCategory(cat.id)}
            whileHover={{ scale: 1.04 }}
            whileTap={{ scale: 0.96 }}
          >
            {activeCategoryId === cat.id && (
              <motion.span
                className="category-bar__active-bg"
                layoutId="category-active"
                transition={{ type: 'spring', stiffness: 400, damping: 30 }}
              />
            )}
            <span className="category-bar__icon">
              {CATEGORY_ICONS[cat.name] || DEFAULT_ICON}
            </span>
            <span className="category-bar__label">{cat.name}</span>
          </motion.button>
        ))}
      </div>
    </div>
  );
}
