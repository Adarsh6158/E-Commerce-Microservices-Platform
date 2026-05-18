import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { api } from './api';

const AuthContext = createContext(null);

function decodeJwtPayload(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    const payload = JSON.parse(json);
    return {
      id: payload.sub,
      email: payload.email,
      roles: payload.roles,
      firstName: payload.firstName,
      lastName: payload.lastName,
      profileImageUrl: payload.profileImageUrl,
    };
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('access_token');
    if (token) {
      const decoded = decodeJwtPayload(token);
      if (decoded) {
        setUser(decoded);
      } else {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
      }
    }
    setLoading(false);
  }, []);

  useEffect(() => {
    const handler = () => {
      setUser(null);
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
    };
    window.addEventListener('auth:logout', handler);
    return () => window.removeEventListener('auth:logout', handler);
  }, []);

  const login = useCallback(async (email, password) => {
    try {
      const data = await api.post('/auth/login', { email, password }, { skipAuth: true });
      localStorage.setItem('access_token', data.accessToken);
      localStorage.setItem('refresh_token', data.refreshToken);
      const decoded = decodeJwtPayload(data.accessToken);
      setUser(decoded || { email });
    } catch (err) {
      const msg = err.body?.message || err.message || '';
      if (err.status === 401 || /invalid credentials/i.test(msg))
        throw new Error('Invalid email or password');
      if (err.status === 403 || /locked/i.test(msg))
        throw new Error('Account locked. Please try again later.');
      if (/not found/i.test(msg))
        throw new Error('No account found with that email');
      throw new Error(msg || 'Login failed. Please try again.');
    }
  }, []);

  const register = useCallback(async (email, password, firstName, lastName) => {
    try {
      const data = await api.post(
        '/auth/register',
        { email, password, firstName, lastName },
        { skipAuth: true }
      );
      localStorage.setItem('access_token', data.accessToken);
      localStorage.setItem('refresh_token', data.refreshToken);
      const decoded = decodeJwtPayload(data.accessToken);
      setUser(decoded || { email });
    } catch (err) {
      const msg = err.body?.message || err.message || '';
      if (/already exists|duplicate/i.test(msg))
        throw new Error('An account with this email already exists');
      if (err.status === 400)
        throw new Error(msg || 'Please check your input and try again');
      throw new Error(msg || 'Registration failed. Please try again.');
    }
  }, []);

  const logout = useCallback(() => {
    api.post('/auth/logout', {}).catch(() => {});
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    setUser(null);
  }, []);

  const updateProfile = useCallback(async (profileData) => {
    const data = await api.put('/auth/profile', profileData);
    setUser((prev) => ({
      ...prev,
      firstName: data.firstName,
      lastName: data.lastName,
      profileImageUrl: data.profileImageUrl,
    }));
    return data;
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        register,
        logout,
        updateProfile,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}