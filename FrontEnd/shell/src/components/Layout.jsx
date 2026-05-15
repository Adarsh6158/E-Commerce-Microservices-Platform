import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuth } from '../lib/auth';
import { onMfeEvent, MfeEvents } from '../lib/events';
import './Layout.css';
export function Layout({ children }) {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const [cartCount, setCartCount] = useState(0);
  const [scrolled, setScrolled] = useState(false);
  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);
  useEffect(() => {
    const loadCartCount = async () => {
      const token = localStorage.getItem('access_token');
      if (!token) {
        setCartCount(0);
        return;
      }
      try {
        const res = await fetch('http://localhost:8080/api/cart', {
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
        });
        if (res.ok) {
          const data = await res.json();
          setCartCount(data?.items?.length || 0);
        }
      } catch {
      }
    };
    loadCartCount();
    return onMfeEvent(MfeEvents.CART_UPDATED, loadCartCount);
  }, [isAuthenticated]);
  const isActive = (path) =>
    location.pathname === path ||
    (path !== '/' && location.pathname.startsWith(path));
  const navItems = [
    { 
      to: '/', 
      label: 'Products', 
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z" />
          <path d="m3.3 7 8.7 5 8.7-5" />
          <path d="M12 22V12" />
        </svg>
      )
    },
    { 
      to: '/search', 
      label: 'Search', 
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="11" cy="11" r="8" />
          <path d="m21 21-4.3-4.3" />
        </svg>
      )
    },
    {
      to: '/cart',
      label: 'Cart',
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="8" cy="21" r="1" />
          <circle cx="19" cy="21" r="1" />
          <path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.1-5.38H5.74" />
        </svg>
      ),
      badge: cartCount > 0 ? cartCount : null,
    },
    ...(isAuthenticated ? [{ 
      to: '/orders', 
      label: 'Orders', 
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z" />
          <polyline points="14 2 14 8 20 8" />
          <line x1="16" y1="13" x2="8" y2="13" />
          <line x1="16" y1="17" x2="8" y2="17" />
          <line x1="10" y1="9" x2="8" y2="9" />
        </svg>
      )
    }] : []),
    ...(user?.roles?.includes('ADMIN')
      ? [{ 
          to: '/admin', 
          label: 'Admin', 
          icon: (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              <path d="m9 12 2 2 4-4" />
            </svg>
          )
        }]
      : []),
  ];
  return (
    <div className="sf-app">
      { }
      <motion.header
        className={`sf-header ${scrolled ? 'sf-header--scrolled' : ''}`}
        initial={{ y: -80 }}
        animate={{ y: 0 }}
        transition={{ type: 'spring', stiffness: 120, damping: 20 }}
      >
        <div className="sf-header__inner">
          { }
          <a
            href="/"
            className="sf-header__logo"
            onClick={(e) => {
              if (location.pathname === '/' && !location.search) {
                e.preventDefault();
                window.scrollTo({ top: 0, behavior: 'smooth' });
              }
            }}
          >
            <motion.div
              className="sf-header__logo-icon"
              whileHover={{ rotate: [0, -5, 5, 0], scale: 1.1 }}
              transition={{ duration: 0.4 }}
            >
              <img src="/logo.png" alt="ShopFlux" width="36" height="36" style={{ objectFit: 'contain' }} />
            </motion.div>
            <span className="sf-header__logo-text">Shop<span className="sf-logo-accent">Flux</span></span>
          </a>
          { }
          <button
            className="sf-header__hamburger"
            onClick={() => setMenuOpen(!menuOpen)}
            aria-label="Toggle menu"
          >
            <motion.span
              animate={{ rotate: menuOpen ? 45 : 0, y: menuOpen ? 6 : 0 }}
              className="sf-hamburger-line"
            />
            <motion.span
              animate={{ opacity: menuOpen ? 0 : 1, scaleX: menuOpen ? 0 : 1 }}
              className="sf-hamburger-line"
            />
            <motion.span
              animate={{ rotate: menuOpen ? -45 : 0, y: menuOpen ? -6 : 0 }}
              className="sf-hamburger-line"
            />
          </button>
          { }
          <nav className={`sf-header__nav ${menuOpen ? 'sf-header__nav--open' : ''}`}>
            {navItems.map(({ to, label, icon, badge }) => (
              <Link
                key={to}
                to={to}
                className={`sf-nav-link ${isActive(to) ? 'sf-nav-link--active' : ''}`}
                onClick={() => setMenuOpen(false)}
              >
                {isActive(to) && (
                  <motion.span
                    className="sf-nav-link__indicator"
                    layoutId="nav-indicator"
                    transition={{ type: 'spring', stiffness: 300, damping: 25 }}
                  />
                )}
                <span className="sf-nav-link__icon">{icon}</span>
                <span className="sf-nav-link__label">{label}</span>
                {badge != null && (
                  <motion.span
                    className="sf-nav-link__badge"
                    initial={{ scale: 0 }}
                    animate={{ scale: 1 }}
                    transition={{ type: 'spring', stiffness: 400, damping: 15 }}
                  >
                    {badge}
                  </motion.span>
                )}
              </Link>
            ))}
          </nav>
          { }
          <div className="sf-header__actions">
            {isAuthenticated && (
              <motion.div whileHover={{ scale: 1.1, rotate: 15 }} whileTap={{ scale: 0.9 }}>
                <Link to="/settings" className="sf-header__action-btn" aria-label="Settings">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                </Link>
              </motion.div>
            )}
            {isAuthenticated ? (
              <>
                <Link to="/profile" className="sf-avatar-link">
                  {user?.profileImageUrl ? (
                    <img
                      src={user.profileImageUrl}
                      alt=""
                      className="sf-avatar sf-avatar--img"
                    />
                  ) : (
                    <motion.div
                      className="sf-avatar"
                      whileHover={{ scale: 1.08 }}
                      whileTap={{ scale: 0.95 }}
                    >
                      {(user?.firstName || user?.email || 'U')[0].toUpperCase()}
                    </motion.div>
                  )}
                </Link>
                <Link to="/profile" className="sf-header__username">
                  {user?.firstName || user?.email}
                </Link>
                <motion.button
                  onClick={() => {
                    logout();
                    navigate('/');
                  }}
                  className="sf-btn-logout"
                  whileHover={{ scale: 1.03 }}
                  whileTap={{ scale: 0.97 }}
                >
                  Sign out
                </motion.button>
              </>
            ) : (
              <motion.div whileHover={{ scale: 1.03 }} whileTap={{ scale: 0.97 }}>
                <Link to="/login" className="sf-btn-signin">
                  Sign in
                </Link>
              </motion.div>
            )}
          </div>
        </div>
      </motion.header>
      { }
      <main className="sf-main">
        <AnimatePresence mode="wait">
          <motion.div
            key={location.pathname.split('/')[1] || 'home'}
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -4 }}
            transition={{ duration: 0.25, ease: [0.16, 1, 0.3, 1] }}
          >
            {children}
          </motion.div>
        </AnimatePresence>
      </main>
      { }
      <footer className="sf-footer">
        <div className="sf-footer__inner">
          <div className="sf-footer__grid">
            <div className="sf-footer__brand">
              <div className="sf-footer__brand-name">
                <img src="/logo.png" alt="" width="28" height="28" style={{ objectFit: 'contain' }} />
                <span>Shop<span className="sf-logo-accent">Flux</span></span>
              </div>
              <p className="sf-footer__brand-desc">
                Your one-stop destination for quality products at great prices.
                Fast shipping and excellent customer service.
              </p>
            </div>
            <div>
              <h4 className="sf-footer__col-title">Shop</h4>
              <ul className="sf-footer__links">
                <li><Link to="/">All Products</Link></li>
                <li><Link to="/brands">Brand List</Link></li>
                <li><Link to="/search">Search</Link></li>
                <li><Link to="/cart">Cart</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="sf-footer__col-title">Account</h4>
              <ul className="sf-footer__links">
                <li><Link to="/login">Sign In</Link></li>
                <li><Link to="/orders">Orders</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="sf-footer__col-title">Support</h4>
              <ul className="sf-footer__links">
                <li><a href="#">Help Center</a></li>
                <li><a href="#">Shipping Info</a></li>
                <li><a href="#">Returns</a></li>
              </ul>
            </div>
          </div>
          <div className="sf-footer__bottom">
            <span>© 2026 ShopFlux. All rights reserved.</span>
            <div className="sf-footer__social">
              <a href="https://twitter.com/your_username" target="_blank" aria-label="Twitter">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M22.46 6c-.77.35-1.5.6-2.3.7a3.92 3.92 0 0 0 1.7-2.2 7.72 7.72 0 0 1-2.5 1A3.9 3.9 0 0 0 16.1 4c-2.2 0-4 1.8-4 4 0 .3 0 .6.1.9A11.1 11.1 0 0 1 3 5.1a4 4 0 0 0-.5 2c0 1.4.7 2.6 1.8 3.3a4 4 0 0 1-1.8-.5v.1c0 2 1.4 3.6 3.2 4a4 4 0 0 1-1.8.1c.5 1.6 2 2.7 3.7 2.8A7.8 7.8 0 0 1 2 19.5 11 11 0 0 0 8 21c7.2 0 11.2-6 11.2-11.2v-.5c.8-.6 1.5-1.3 2-2.3z" />
                </svg>
              </a>
              <a href="https://github.com/your_username" target="_blank" aria-label="GitHub">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 .5C5.7.5.7 5.6.7 12c0 5.1 3.3 9.4 7.9 10.9.6.1.8-.3.8-.6v-2.2c-3.2.7-3.9-1.5-3.9-1.5-.5-1.3-1.2-1.6-1.2-1.6-1-.7.1-.7.1-.7 1.1.1 1.7 1.2 1.7 1.2 1 .1 2-.7 2.4-1.1.1-.7.4-1.2.7-1.5-2.6-.3-5.3-1.3-5.3-5.8 0-1.3.5-2.3 1.2-3.2-.1-.3-.5-1.5.1-3.1 0 0 1-.3 3.2 1.2a11 11 0 0 1 5.8 0C17.2 2.7 18.2 3 18.2 3c.6 1.6.2 2.8.1 3.1.8.9 1.2 2 1.2 3.2 0 4.5-2.7 5.5-5.3 5.8.4.4.8 1 .8 2v3c0 .3.2.7.8.6A11.3 11.3 0 0 0 23.3 12c0-6.4-5-11.5-11.3-11.5z" />
                </svg>
              </a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}