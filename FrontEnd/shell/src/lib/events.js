/**
 * Cross-MFE Event Bus
 * Enables communication between micro-frontends via custom DOM events.
 */

export const MfeEvents = {
  CART_UPDATED: 'mfe:cart:updated',
  CART_ADD: 'mfe:cart:add',
  AUTH_CHANGED: 'mfe:auth:changed',
  NAVIGATE: 'mfe:navigate',
  PRODUCT_VIEW: 'mfe:product:view',
  ORDER_PLACED: 'mfe:order:placed',
  TOAST: 'mfe:toast',
};

export function emitMfeEvent(type, detail = {}) {
  window.dispatchEvent(new CustomEvent(type, { detail }));
}

export function onMfeEvent(type, handler) {
  const listener = (e) => handler(e.detail);
  window.addEventListener(type, listener);
  return () => window.removeEventListener(type, listener);
}