import React, { useState, useMemo, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../lib/api';
import './BrandsPage.css';

export function BrandsPage() {
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeLetter, setActiveLetter] = useState('All');
  
  useEffect(() => {
    api.get('/products/brands')
      .then(data => {
        setBrands(data || []);
      })
      .catch(err => {
        console.error('Failed to fetch brands:', err);
      })
      .finally(() => setLoading(false));
  }, []);
  
  const alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
  
  const groupedBrands = useMemo(() => {
    const groups = {};
    [...brands].sort().forEach(brand => {
      const firstLetter = brand[0].toUpperCase();
      if (!groups[firstLetter]) groups[firstLetter] = [];
      groups[firstLetter].push(brand);
    });
    return groups;
  }, [brands]);

  const sectionRefs = useRef({});

  const scrollToSection = (letter) => {
    setActiveLetter(letter);
    if (letter === 'All') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else if (sectionRefs.current[letter]) {
      const offset = 100;
      const bodyRect = document.body.getBoundingClientRect().top;
      const elementRect = sectionRefs.current[letter].getBoundingClientRect().top;
      const elementPosition = elementRect - bodyRect;
      const offsetPosition = elementPosition - offset;

      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
      });
    }
  };

  const filteredLetters = activeLetter === 'All' 
    ? Object.keys(groupedBrands).sort()
    : [activeLetter].filter(l => groupedBrands[l]);

  if (loading) {
    return (
      <div className="brands-page-loading">
        <div className="spinner"></div>
        <p>Loading brands...</p>
      </div>
    );
  }

  return (
    <div className="brands-page">
      <div className="brands-hero">
        <h1 className="brands-hero__title">Explore Our Brands</h1>
        <p className="brands-hero__subtitle">Find your favorite brands from A to Z</p>
      </div>

      <div className="brands-nav-wrapper">
        <nav className="brands-alphabet-nav">
          <button 
            className={`alphabet-btn ${activeLetter === 'All' ? 'active' : ''}`}
            onClick={() => scrollToSection('All')}
          >
            All
          </button>
          {alphabet.map(letter => (
            <button
              key={letter}
              disabled={!groupedBrands[letter]}
              className={`alphabet-btn ${activeLetter === letter ? 'active' : ''}`}
              onClick={() => scrollToSection(letter)}
            >
              {letter}
            </button>
          ))}
        </nav>
      </div>

      <div className="brands-list-container">
        {filteredLetters.map(letter => (
          <section 
            key={letter} 
            className="brand-section"
            ref={el => sectionRefs.current[letter] = el}
          >
            <div className="brand-section__letter-badge">{letter}</div>
            <div className="brand-grid">
              {groupedBrands[letter].map(brand => (
                <Link 
                  key={brand} 
                  to={`/products?brand=${encodeURIComponent(brand)}`}
                  className="brand-card-link"
                >
                  <div className="brand-card">
                    <div className="brand-card__content">
                      <span className="brand-card__name">{brand}</span>
                      <div className="brand-card__arrow">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M5 12h14m-7-7 7 7-7 7"/>
                        </svg>
                      </div>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          </section>
        ))}
        
        {filteredLetters.length === 0 && (
          <div className="brands-empty">
            <p>No brands found for "{activeLetter}"</p>
            <button className="btn-reset" onClick={() => scrollToSection('All')}>View All Brands</button>
          </div>
        )}
      </div>
    </div>
  );
}
