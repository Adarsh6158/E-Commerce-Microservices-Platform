import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuth } from '../lib/auth';
import './SettingsPage.css';

const TABS = [
  { id: 'general', label: 'General', icon: '👤' },
  { id: 'theme', label: 'Theme & Style', icon: '🎨' },
  { id: 'language', label: 'Language', icon: '🌐' },
  { id: 'address', label: 'Addresses', icon: '📍' },
  { id: 'payment', label: 'Payments', icon: '💳' },
  { id: 'security', label: 'Security', icon: '🔒' },
  { id: 'privacy', label: 'Privacy', icon: '🛡️' }
];

export function SettingsPage() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('general');
  const [theme, setTheme] = useState(document.documentElement.getAttribute('data-theme') || 'light');
  const [language, setLanguage] = useState('en');

  // Apply theme globally
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('sf-theme', theme);
  }, [theme]);

  const renderContent = () => {
    switch (activeTab) {
      case 'general':
        return (
          <motion.div 
            initial={{ opacity: 0, x: 10 }} 
            animate={{ opacity: 1, x: 0 }} 
            className="settings-section"
          >
            <h2 className="section-title">General Preferences</h2>
            <div className="settings-form">
              <div className="form-group">
                <label>Full Name</label>
                <input type="text" defaultValue={user?.firstName + ' ' + user?.lastName} placeholder="Enter your name" />
              </div>
              <div className="form-group">
                <label>Email Address</label>
                <input type="email" defaultValue={user?.email} disabled className="input-disabled" />
                <span className="input-hint">Email cannot be changed for security reasons.</span>
              </div>
              <div className="form-group">
                <label>Phone Number</label>
                <div className="input-with-prefix">
                  <span className="prefix">+91</span>
                  <input type="tel" placeholder="98765 43210" maxLength="10" />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Notification Alerts</label>
                  <label className="sf-toggle">
                    <input type="checkbox" defaultChecked />
                    <span className="sf-toggle-slider"></span>
                  </label>
                </div>
              </div>
              <button className="sf-btn-primary">Save Changes</button>
            </div>
          </motion.div>
        );
      case 'theme':
        return (
          <motion.div 
            initial={{ opacity: 0, x: 10 }} 
            animate={{ opacity: 1, x: 0 }} 
            className="settings-section"
          >
            <h2 className="section-title">Theme & Appearance</h2>
            <div className="theme-grid">
              <div 
                className={`theme-card light ${theme === 'light' ? 'active' : ''}`}
                onClick={() => setTheme('light')}
              >
                <div className="theme-card__preview"></div>
                <span className="theme-card__label">Light Mode</span>
              </div>
              <div 
                className={`theme-card dark ${theme === 'dark' ? 'active' : ''}`}
                onClick={() => setTheme('dark')}
              >
                <div className="theme-card__preview"></div>
                <span className="theme-card__label">Dark Mode</span>
              </div>
              <div 
                className={`theme-card glass ${theme === 'glass' ? 'active' : ''}`}
                onClick={() => setTheme('glass')}
              >
                <div className="theme-card__preview"></div>
                <span className="theme-card__label">Glassmorphism</span>
              </div>
            </div>
          </motion.div>
        );
      case 'language':
        return (
          <motion.div 
            initial={{ opacity: 0, x: 10 }} 
            animate={{ opacity: 1, x: 0 }} 
            className="settings-section"
          >
            <h2 className="section-title">Language Preferences</h2>
            <div className="settings-form">
              <div className="form-group">
                <label>Select System Language</label>
                <select 
                  className="sf-select" 
                  value={language} 
                  onChange={(e) => setLanguage(e.target.value)}
                >
                  <option value="en">English (US)</option>
                  <option value="es">Español</option>
                  <option value="fr">Français</option>
                  <option value="de">Deutsch</option>
                  <option value="hi">हिन्दी</option>
                </select>
              </div>
              <button className="sf-btn-primary">Update Language</button>
            </div>
          </motion.div>
        );
      case 'address':
        return (
          <motion.div 
            initial={{ opacity: 0, x: 10 }} 
            animate={{ opacity: 1, x: 0 }} 
            className="settings-section"
          >
            <div className="section-header">
              <h2 className="section-title">Saved Addresses</h2>
              <button className="sf-btn-outline sf-btn-sm">+ Add New</button>
            </div>
            <div className="address-list">
              <div className="address-card active">
                <div className="address-card__header">
                  <span className="address-tag">Default</span>
                  <div className="address-actions">
                    <button>Edit</button>
                    <button className="delete">Remove</button>
                  </div>
                </div>
                <p className="address-text">123 ShopFlux Avenue, Suite 100<br/>San Francisco, CA 94103</p>
              </div>
              <div className="address-card">
                <div className="address-card__header">
                  <span className="address-tag">Work</span>
                  <div className="address-actions">
                    <button>Edit</button>
                    <button className="delete">Remove</button>
                  </div>
                </div>
                <p className="address-text">456 Tech Plaza, Floor 12<br/>New York, NY 10001</p>
              </div>
            </div>
          </motion.div>
        );
      case 'payment':
        return (
          <motion.div 
            initial={{ opacity: 0, x: 10 }} 
            animate={{ opacity: 1, x: 0 }} 
            className="settings-section"
          >
            <div className="section-header">
              <h2 className="section-title">Payment Methods</h2>
              <button className="sf-btn-outline sf-btn-sm">+ Add Card</button>
            </div>
            <div className="payment-list">
              <div className="payment-card">
                <div className="payment-card__icon">💳</div>
                <div className="payment-card__info">
                  <span className="card-number">•••• •••• •••• 4242</span>
                  <span className="card-expiry">Expires 12/28</span>
                </div>
                <span className="payment-tag primary">Primary</span>
              </div>
              <div className="payment-card">
                <div className="payment-card__icon">📱</div>
                <div className="payment-card__info">
                  <span className="card-number">adarsh@okaxis</span>
                  <span className="card-expiry">UPI ID</span>
                </div>
              </div>
            </div>
          </motion.div>
        );
      case 'security':
        return (
          <motion.div 
            initial={{ opacity: 0, x: 10 }} 
            animate={{ opacity: 1, x: 0 }} 
            className="settings-section"
          >
            <h2 className="section-title">Security & Password</h2>
            <div className="settings-form">
              <div className="form-group">
                <label>Current Password</label>
                <input type="password" placeholder="••••••••" />
              </div>
              <div className="form-group">
                <label>New Password</label>
                <input type="password" placeholder="Enter new password" />
              </div>
              <div className="form-group">
                <label>Confirm New Password</label>
                <input type="password" placeholder="Confirm new password" />
              </div>
              <div className="security-status">
                <span className="status-dot green"></span>
                Two-Factor Authentication is enabled.
              </div>
              <button className="sf-btn-primary">Update Password</button>
            </div>
          </motion.div>
        );
      case 'privacy':
        return (
          <motion.div 
            initial={{ opacity: 0, x: 10 }} 
            animate={{ opacity: 1, x: 0 }} 
            className="settings-section"
          >
            <h2 className="section-title">Privacy & Data Control</h2>
            <div className="settings-form">
              <div className="form-row-check">
                <label className="sf-toggle">
                  <input type="checkbox" defaultChecked />
                  <span className="sf-toggle-slider"></span>
                </label>
                <div className="check-info">
                  <span className="check-label">Share usage data</span>
                  <span className="check-desc">Help us improve ShopFlux by sharing anonymous performance metrics.</span>
                </div>
              </div>
              <div className="form-row-check">
                <label className="sf-toggle">
                  <input type="checkbox" defaultChecked />
                  <span className="sf-toggle-slider"></span>
                </label>
                <div className="check-info">
                  <span className="check-label">Personalized Recommendations</span>
                  <span className="check-desc">Use my browsing history to suggest products I might like.</span>
                </div>
              </div>
              <button className="sf-btn-outline" style={{ color: '#ef4444', borderColor: '#ef4444' }}>Request Data Deletion</button>
            </div>
          </motion.div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="settings-page">
      <div className="settings-container">
        <header className="settings-header">
          <h1 className="settings-title">Account Settings</h1>
          <p className="settings-subtitle">Manage your account preferences, themes, and personal details.</p>
        </header>

        <div className="settings-grid">
          <aside className="settings-nav">
            {TABS.map(tab => (
              <button 
                key={tab.id}
                className={`settings-nav-item ${activeTab === tab.id ? 'active' : ''}`}
                onClick={() => setActiveTab(tab.id)}
              >
                <span className="tab-icon">{tab.icon}</span>
                <span className="tab-label">{tab.label}</span>
                {activeTab === tab.id && (
                  <motion.div 
                    layoutId="activeTab" 
                    className="active-indicator" 
                  />
                )}
              </button>
            ))}
          </aside>

          <main className="settings-content">
            <AnimatePresence mode="wait">
              {renderContent()}
            </AnimatePresence>
          </main>
        </div>
      </div>
    </div>
  );
}
