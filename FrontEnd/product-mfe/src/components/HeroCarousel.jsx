import React, { useState, useEffect, useRef, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
const BANNERS = [
  {
    id: 1,
    gradient: 'linear-gradient(135deg, hsl(240, 8%, 8%) 0%, hsl(220, 25%, 14%) 50%, hsl(210, 50%, 20%) 100%)',
    title: 'Mega Electronics Sale',
    subtitle: 'Up to 70% off on top brands',
    badge: 'LIMITED TIME',
    accent: 'hsl(210, 100%, 72%)',
    categoryName: 'Electronics',
  },
  {
    id: 2,
    gradient: 'linear-gradient(135deg, hsl(240, 20%, 16%) 0%, hsl(280, 40%, 30%) 50%, hsl(346, 84%, 48%) 100%)',
    title: 'Fashion Fiesta',
    subtitle: 'New arrivals starting ₹299',
    badge: 'TRENDING',
    accent: 'hsl(330, 80%, 72%)',
    categoryName: 'Clothing',
  },
  {
    id: 3,
    gradient: 'linear-gradient(135deg, hsl(168, 70%, 25%) 0%, hsl(145, 65%, 50%) 100%)',
    title: 'Home & Kitchen Deals',
    subtitle: 'Refresh your space – flat 50% off',
    badge: 'BEST SELLERS',
    accent: '#fff',
    categoryName: 'Home & Kitchen',
  },
  {
    id: 4,
    gradient: 'linear-gradient(135deg, hsl(10, 85%, 45%) 0%, hsl(40, 90%, 52%) 100%)',
    title: 'Sports & Fitness',
    subtitle: 'Gear up for summer – extra 20% off',
    badge: 'NEW LAUNCH',
    accent: '#fff',
    categoryName: 'Sports & Outdoors',
  },
  {
    id: 5,
    gradient: 'linear-gradient(135deg, hsl(235, 70%, 66%) 0%, hsl(272, 40%, 52%) 100%)',
    title: 'Beauty Bestsellers',
    subtitle: 'Premium skincare & makeup from ₹199',
    badge: 'TOP PICKS',
    accent: 'hsl(320, 80%, 85%)',
    categoryName: 'Beauty & Health',
  },
];
const slideVariants = {
  enter: (dir) => ({ x: dir > 0 ? 60 : -60, opacity: 0 }),
  center: { x: 0, opacity: 1 },
  exit: (dir) => ({ x: dir > 0 ? -60 : 60, opacity: 0 }),
};
export function HeroCarousel({ categories, onSelectCategory }) {
  const [current, setCurrent] = useState(0);
  const [paused, setPaused] = useState(false);
  const [direction, setDirection] = useState(1);
  const timerRef = useRef(null);
  const touchStartX = useRef(0);
  const goTo = useCallback((idx, dir = 1) => {
    setDirection(dir);
    setCurrent((idx + BANNERS.length) % BANNERS.length);
  }, []);
  useEffect(() => {
    if (paused) return;
    timerRef.current = setInterval(() => goTo(current + 1, 1), 5000);
    return () => clearInterval(timerRef.current);
  }, [current, paused, goTo]);
  const handleTouchStart = (e) => { touchStartX.current = e.touches[0].clientX; };
  const handleTouchEnd = (e) => {
    const diff = touchStartX.current - e.changedTouches[0].clientX;
    if (Math.abs(diff) > 50) goTo(current + (diff > 0 ? 1 : -1), diff > 0 ? 1 : -1);
  };
  const banner = BANNERS[current];
  const handleShopNow = () => {
    const catName = banner.categoryName;
    if (!catName || !categories?.length || !onSelectCategory) return;
    const match = categories.find(c => c.name === catName);
    if (match) onSelectCategory(match.id);
  };
  return (
    <div
      className="hero-carousel"
      onMouseEnter={() => setPaused(true)}
      onMouseLeave={() => setPaused(false)}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      {}
      <motion.div
        className="hero-carousel__bg"
        animate={{ background: banner.gradient }}
        transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
      />
      {}
      <div className="hero-carousel__orbs">
        <motion.div
          className="hero-carousel__orb hero-carousel__orb--1"
          animate={{ x: [0, 20, 0], y: [0, -15, 0] }}
          transition={{ duration: 8, repeat: Infinity, ease: 'easeInOut' }}
        />
        <motion.div
          className="hero-carousel__orb hero-carousel__orb--2"
          animate={{ x: [0, -15, 0], y: [0, 20, 0] }}
          transition={{ duration: 10, repeat: Infinity, ease: 'easeInOut' }}
        />
      </div>
      {}
      <AnimatePresence mode="wait" custom={direction}>
        <motion.div
          key={current}
          className="hero-carousel__slide"
          custom={direction}
          variants={slideVariants}
          initial="enter"
          animate="center"
          exit="exit"
          transition={{ duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
        >
          <motion.span
            className="hero-carousel__badge"
            style={{ color: banner.accent }}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1, duration: 0.4, ease: [0.16, 1, 0.3, 1] }}
          >
            {banner.badge}
          </motion.span>
          <motion.h2
            className="hero-carousel__title"
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.15, duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
          >
            {banner.title}
          </motion.h2>
          <motion.p
            className="hero-carousel__subtitle"
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.22, duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
          >
            {banner.subtitle}
          </motion.p>
          <motion.button
            className="hero-carousel__cta"
            onClick={handleShopNow}
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3, duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
            whileHover={{ scale: 1.04, boxShadow: '0 8px 24px hsla(0,0%,0%,0.2)' }}
            whileTap={{ scale: 0.97 }}
          >
            Shop Now
            <svg width="14" height="14" viewBox="0 0 16 16" fill="none" style={{ marginLeft: 6 }}>
              <path d="M3 8h10m0 0L9 4m4 4L9 12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </motion.button>
        </motion.div>
      </AnimatePresence>
      {}
      <motion.button
        className="hero-carousel__arrow hero-carousel__arrow--left"
        onClick={() => goTo(current - 1, -1)}
        aria-label="Previous slide"
        whileHover={{ scale: 1.1, background: 'hsla(0,0%,100%,0.25)' }}
        whileTap={{ scale: 0.9 }}
      >
        ‹
      </motion.button>
      <motion.button
        className="hero-carousel__arrow hero-carousel__arrow--right"
        onClick={() => goTo(current + 1, 1)}
        aria-label="Next slide"
        whileHover={{ scale: 1.1, background: 'hsla(0,0%,100%,0.25)' }}
        whileTap={{ scale: 0.9 }}
      >
        ›
      </motion.button>
      {}
      <div className="hero-carousel__dots">
        {BANNERS.map((_, i) => (
          <motion.button
            key={i}
            className={`hero-carousel__dot ${i === current ? 'hero-carousel__dot--active' : ''}`}
            onClick={() => goTo(i, i > current ? 1 : -1)}
            aria-label={`Go to slide ${i + 1}`}
            whileHover={{ scale: 1.3 }}
            whileTap={{ scale: 0.85 }}
            layout
          />
        ))}
      </div>
    </div>
  );
}
