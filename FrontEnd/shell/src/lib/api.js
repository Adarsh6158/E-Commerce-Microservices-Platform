const API_BASE = 'http://localhost:8080/api';

class ApiClient {
  getToken() {
    return localStorage.getItem('access_token');
  }

  async request(path, options = {}) {
    const { skipAuth = false, headers: extraHeaders, ...rest } = options;

    const headers = {
      'Content-Type': 'application/json',
      ...extraHeaders,
    };

    if (!skipAuth) {
      const token = this.getToken();
      if (token) headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(`${API_BASE}${path}`, {
      ...rest,
      headers,
      credentials: 'include',
    });

    if (res.status === 401 && !skipAuth) {
      const refreshed = await this.refreshToken();
      if (refreshed) return this.request(path, options);

      window.dispatchEvent(new Event('auth:logout'));
      throw new ApiError(401, 'Session expired');
    }

    if (!res.ok) {
      const body = await res.json().catch(() => ({ message: res.statusText }));
      throw new ApiError(res.status, body.message || res.statusText, body);
    }

    if (res.status === 204) return undefined;
    return res.json();
  }

  async refreshToken() {
    const refreshToken = localStorage.getItem('refresh_token');
    if (!refreshToken) return false;

    try {
      const res = await fetch(`${API_BASE}/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });

      if (!res.ok) return false;

      const data = await res.json();
      localStorage.setItem('access_token', data.accessToken);
      localStorage.setItem('refresh_token', data.refreshToken);

      return true;
    } catch {
      return false;
    }
  }

  get(path, opts) {
    return this.request(path, { ...opts, method: 'GET' });
  }

  post(path, body, opts) {
    return this.request(path, {
      ...opts,
      method: 'POST',
      body: JSON.stringify(body),
    });
  }

  put(path, body, opts) {
    return this.request(path, {
      ...opts,
      method: 'PUT',
      body: JSON.stringify(body),
    });
  }

  delete(path, opts) {
    return this.request(path, { ...opts, method: 'DELETE' });
  }
}

export class ApiError extends Error {
  constructor(status, message, body) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

export const api = new ApiClient();