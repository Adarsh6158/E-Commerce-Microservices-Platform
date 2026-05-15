import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../lib/auth';
import './SettingsPage.css';

export function SettingsPage() {
  const { t, i18n } = useTranslation();
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('general');
  const [theme, setTheme] = useState(document.documentElement.getAttribute('data-theme') || 'light');
  
  // Language States
  const [currentLanguage, setCurrentLanguage] = useState(i18n.language || 'en');
  const [pendingLanguage, setPendingLanguage] = useState(i18n.language || 'en');

  // Address States
  const [addresses, setAddresses] = useState([
    { id: 1, tag: 'Home', street: '123 ShopFlux Avenue, Suite 100', city: 'San Francisco', state: 'CA', zip: '94103', isDefault: true },
    { id: 2, tag: 'Office', street: '456 Tech Plaza, Floor 12', city: 'New York', state: 'NY', zip: '10001', isDefault: false }
  ]);
  const [isEditingAddress, setIsEditingAddress] = useState(false);
  const [currentEditAddress, setCurrentEditAddress] = useState(null);

  // Payment States
  const [paymentMethods, setPaymentMethods] = useState([
    { id: 1, type: 'card', brand: 'visa', last4: '4242', expiry: '12/28', holder: 'Adarsh Kumar', isPrimary: true },
    { id: 2, type: 'card', brand: 'mastercard', last4: '8888', expiry: '09/26', holder: 'Adarsh Kumar', isPrimary: false },
    { id: 3, type: 'upi', provider: 'googlepay', upiId: 'adarsh@okaxis', isPrimary: false }
  ]);

  const TABS = [
    { id: 'general', label: t('general'), icon: '👤' },
    { id: 'theme', label: t('theme_style'), icon: '🎨' },
    { id: 'language', label: t('language'), icon: '🌐' },
    { id: 'address', label: t('addresses'), icon: '📍' },
    { id: 'payment', label: t('payments'), icon: '💳' },
    { id: 'security', label: t('security'), icon: '🔒' },
    { id: 'privacy', label: t('privacy'), icon: '🛡️' }
  ];

  // Apply theme globally
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('sf-theme', theme);
  }, [theme]);

  const handleLanguageUpdate = () => {
    i18n.changeLanguage(pendingLanguage);
    setCurrentLanguage(pendingLanguage);
  };

  const handleAddAddress = () => {
    setCurrentEditAddress({ tag: '', street: '', city: '', state: '', zip: '', isDefault: false });
    setIsEditingAddress(true);
  };

  const handleEditAddress = (addr) => {
    setCurrentEditAddress({ ...addr });
    setIsEditingAddress(true);
  };

  const handleDeleteAddress = (id) => {
    setAddresses(addresses.filter(a => a.id !== id));
  };

  const handleSaveAddress = (e) => {
    e.preventDefault();
    if (currentEditAddress.id) {
      setAddresses(addresses.map(a => a.id === currentEditAddress.id ? currentEditAddress : a));
    } else {
      setAddresses([...addresses, { ...currentEditAddress, id: Date.now() }]);
    }
    setIsEditingAddress(false);
    setCurrentEditAddress(null);
  };

  const renderContent = () => {
    switch (activeTab) {
      case 'general':
        return (
          <motion.div initial={{ opacity: 0, x: 10 }} animate={{ opacity: 1, x: 0 }} className="settings-section">
            <h2 className="section-title">{t('general')}</h2>
            <div className="settings-form">
              <div className="form-group">
                <label>{t('full_name')}</label>
                <input type="text" defaultValue={user?.firstName + ' ' + (user?.lastName || '')} placeholder="Enter your name" />
              </div>
              <div className="form-group">
                <label>{t('email_address')}</label>
                <input type="email" defaultValue={user?.email} disabled className="input-disabled" />
                <span className="input-hint">Email cannot be changed for security reasons.</span>
              </div>
              <div className="form-group">
                <label>{t('phone_number')}</label>
                <div className="input-with-prefix">
                  <span className="prefix">+91</span>
                  <input type="tel" placeholder="98765 43210" maxLength="10" />
                </div>
              </div>
              <button className="sf-btn-primary">{t('save_changes')}</button>
            </div>
          </motion.div>
        );
      case 'theme':
        return (
          <motion.div initial={{ opacity: 0, x: 10 }} animate={{ opacity: 1, x: 0 }} className="settings-section">
            <h2 className="section-title">{t('theme_style')}</h2>
            <div className="theme-grid">
              {['light', 'dark', 'glass'].map(tName => (
                <div 
                  key={tName}
                  className={`theme-card ${tName} ${theme === tName ? 'active' : ''}`}
                  onClick={() => setTheme(tName)}
                >
                  <div className="theme-card__preview"></div>
                  <span className="theme-card__label">{t(tName + '_mode')}</span>
                </div>
              ))}
            </div>
          </motion.div>
        );
      case 'language':
        return (
          <motion.div initial={{ opacity: 0, x: 10 }} animate={{ opacity: 1, x: 0 }} className="settings-section">
            <h2 className="section-title">{t('language')}</h2>
            <div className="settings-form">
              <div className="form-group">
                <label>{t('select_language')}</label>
                <select 
                  className="sf-select" 
                  value={pendingLanguage} 
                  onChange={(e) => setPendingLanguage(e.target.value)}
                >
                  <option value="en">English (US)</option>
                  <option value="es">Español</option>
                  <option value="fr">Français</option>
                  <option value="de">Deutsch</option>
                  <option value="hi">हिन्दी (Hindi)</option>
                  <option value="kn">ಕನ್ನಡ (Kannada)</option>
                  <option value="ta">தமிழ் (Tamil)</option>
                  <option value="te">తెలుగు (Telugu)</option>
                  <option value="bn">বাংলা (Bengali)</option>
                </select>
              </div>
              <div className="language-status">
                {currentLanguage !== pendingLanguage ? (
                  <p className="pending-hint">Click update to apply changes</p>
                ) : (
                  <p className="active-hint">Current: {currentLanguage}</p>
                )}
              </div>
              <button 
                className="sf-btn-primary" 
                onClick={handleLanguageUpdate}
                disabled={currentLanguage === pendingLanguage}
                style={{ opacity: currentLanguage === pendingLanguage ? 0.6 : 1 }}
              >
                {t('update_language')}
              </button>
            </div>
          </motion.div>
        );
      case 'address':
        return (
          <motion.div initial={{ opacity: 0, x: 10 }} animate={{ opacity: 1, x: 0 }} className="settings-section">
            <div className="section-header">
              <h2 className="section-title">{t('addresses')}</h2>
              <button className="sf-btn-add" onClick={handleAddAddress}>
                <span className="plus-icon">+</span> {t('add_new')}
              </button>
            </div>

            <AnimatePresence mode="wait">
              {isEditingAddress ? (
                <motion.form 
                  key="addr-form"
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -20 }}
                  className="address-form"
                  onSubmit={handleSaveAddress}
                >
                  <div className="form-grid">
                    <div className="form-group">
                      <label>Label (e.g. Home, Office)</label>
                      <input 
                        type="text" 
                        value={currentEditAddress.tag} 
                        onChange={e => setCurrentEditAddress({...currentEditAddress, tag: e.target.value})}
                        required 
                      />
                    </div>
                    <div className="form-group span-2">
                      <label>Street Address</label>
                      <input 
                        type="text" 
                        value={currentEditAddress.street} 
                        onChange={e => setCurrentEditAddress({...currentEditAddress, street: e.target.value})}
                        required 
                      />
                    </div>
                    <div className="form-group">
                      <label>City</label>
                      <input 
                        type="text" 
                        value={currentEditAddress.city} 
                        onChange={e => setCurrentEditAddress({...currentEditAddress, city: e.target.value})}
                        required 
                      />
                    </div>
                    <div className="form-group">
                      <label>State</label>
                      <input 
                        type="text" 
                        value={currentEditAddress.state} 
                        onChange={e => setCurrentEditAddress({...currentEditAddress, state: e.target.value})}
                        required 
                      />
                    </div>
                    <div className="form-group">
                      <label>ZIP Code</label>
                      <input 
                        type="text" 
                        value={currentEditAddress.zip} 
                        onChange={e => setCurrentEditAddress({...currentEditAddress, zip: e.target.value})}
                        required 
                      />
                    </div>
                  </div>
                  <div className="form-actions">
                    <button type="button" className="sf-btn-outline" onClick={() => setIsEditingAddress(false)}>Cancel</button>
                    <button type="submit" className="sf-btn-primary">Save Address</button>
                  </div>
                </motion.form>
              ) : (
                <motion.div key="addr-list" className="address-grid">
                  {addresses.map(addr => (
                    <div key={addr.id} className={`address-premium-card ${addr.isDefault ? 'is-default' : ''}`}>
                      <div className="card-top">
                        <span className="address-badge">{addr.tag}</span>
                        <div className="card-actions">
                          <button onClick={() => handleEditAddress(addr)} title="Edit">✏️</button>
                          <button onClick={() => handleDeleteAddress(addr.id)} className="delete" title="Delete">🗑️</button>
                        </div>
                      </div>
                      <div className="card-content">
                        <p className="street">{addr.street}</p>
                        <p className="location">{addr.city}, {addr.state} {addr.zip}</p>
                      </div>
                      {addr.isDefault && <div className="default-indicator">Default Shipping Address</div>}
                    </div>
                  ))}
                </motion.div>
              )}
            </AnimatePresence>
          </motion.div>
        );
      case 'payment':
        return (
          <motion.div initial={{ opacity: 0, x: 10 }} animate={{ opacity: 1, x: 0 }} className="settings-section">
            <div className="section-header">
              <h2 className="section-title">{t('payment_methods')}</h2>
              <button className="sf-btn-primary sf-btn-sm">+ Add New</button>
            </div>
            
            <div className="payment-premium-container">
              <div className="payment-group">
                <h3 className="payment-group-title">Saved Cards</h3>
                <div className="wallet-grid">
                  {paymentMethods.filter(p => p.type === 'card').map(card => (
                    <div key={card.id} className={`premium-card-item ${card.brand}`}>
                      <div className="card-inner">
                        <div className="card-brand-logo">
                          {card.brand === 'visa' ? 'VISA' : 'mastercard'}
                        </div>
                        <div className="card-chip"></div>
                        <div className="card-number-display">•••• •••• •••• {card.last4}</div>
                        <div className="card-footer">
                          <div className="card-holder">{user?.firstName} {user?.lastName}</div>
                          <div className="card-expiry">{card.expiry}</div>
                        </div>
                      </div>
                      {card.isPrimary && <div className="primary-badge">PRIMARY</div>}
                    </div>
                  ))}
                  <div className="add-card-placeholder">
                    <span className="plus">+</span>
                    <span>Add Credit/Debit Card</span>
                  </div>
                </div>
              </div>

              <div className="payment-group">
                <h3 className="payment-group-title">UPI IDs</h3>
                <div className="upi-list">
                  {paymentMethods.filter(p => p.type === 'upi').map(upi => (
                    <div key={upi.id} className="upi-item">
                      <div className="upi-icon-box">
                        <img src="https://upload.wikimedia.org/wikipedia/commons/e/e1/UPI-Logo-vector.svg" alt="UPI" />
                      </div>
                      <div className="upi-info">
                        <span className="upi-id-text">{upi.upiId}</span>
                        <span className="upi-sub">Verified Payment ID</span>
                      </div>
                      <button className="upi-action-btn">Remove</button>
                    </div>
                  ))}
                  <div className="add-upi-box">
                    <input type="text" placeholder="Enter new UPI ID (e.g. user@bank)" />
                    <button className="upi-add-btn">Add ID</button>
                  </div>
                </div>
              </div>
            </div>
          </motion.div>
        );
      case 'security':
        return (
          <motion.div initial={{ opacity: 0, x: 10 }} animate={{ opacity: 1, x: 0 }} className="settings-section">
            <h2 className="section-title">{t('security')}</h2>
            <div className="settings-form">
              <div className="form-group">
                <label>Current Password</label>
                <input type="password" placeholder="••••••••" />
              </div>
              <div className="form-group">
                <label>New Password</label>
                <input type="password" placeholder="Enter new password" />
              </div>
              <button className="sf-btn-primary">{t('update_password')}</button>
            </div>
          </motion.div>
        );
      case 'privacy':
        return (
          <motion.div initial={{ opacity: 0, x: 10 }} animate={{ opacity: 1, x: 0 }} className="settings-section">
            <h2 className="section-title">{t('privacy')}</h2>
            <div className="privacy-premium-container">
              <div className="privacy-card">
                <div className="privacy-card-header">
                  <div className="privacy-icon">📊</div>
                  <div className="privacy-title-box">
                    <span className="privacy-label">Usage Analytics</span>
                    <span className="privacy-desc">Share anonymous usage data to help us improve the experience.</span>
                  </div>
                  <label className="sf-toggle">
                    <input type="checkbox" defaultChecked />
                    <span className="sf-toggle-slider"></span>
                  </label>
                </div>
              </div>

              <div className="privacy-card">
                <div className="privacy-card-header">
                  <div className="privacy-icon">🔔</div>
                  <div className="privacy-title-box">
                    <span className="privacy-label">Personalized Offers</span>
                    <span className="privacy-desc">Receive tailored product recommendations based on your browsing.</span>
                  </div>
                  <label className="sf-toggle">
                    <input type="checkbox" />
                    <span className="sf-toggle-slider"></span>
                  </label>
                </div>
              </div>

              <div className="privacy-danger-zone">
                <h3 className="danger-title">Danger Zone</h3>
                <p className="danger-desc">Once you delete your data, it cannot be recovered. Please proceed with caution.</p>
                <button className="sf-btn-danger">
                  <span className="icon">🗑️</span> Request Data Deletion
                </button>
              </div>
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
          <h1 className="settings-title">{t('account_settings')}</h1>
          <p className="settings-subtitle">{t('manage_account')}</p>
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
                  <motion.div layoutId="activeTab" className="active-indicator" />
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
