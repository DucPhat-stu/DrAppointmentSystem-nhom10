/* =========================================================
   HTTP Client - Auto-attach JWT, refresh once on 401, parse API envelope
   ========================================================= */

import { clearSession, loadSession, updateAccessToken } from './sessionStorageService.js';

const SERVICE_URLS = {
  auth: import.meta.env.VITE_AUTH_URL ?? 'http://localhost:8086',
  user: import.meta.env.VITE_USER_URL ?? 'http://localhost:8082',
  doctor: import.meta.env.VITE_DOCTOR_URL ?? 'http://localhost:8083',
  appointment: import.meta.env.VITE_APPOINTMENT_URL ?? 'http://localhost:8084',
  notification: import.meta.env.VITE_NOTIFICATION_URL ?? 'http://localhost:8085',
};

function buildUrl(service, path) {
  const base = SERVICE_URLS[service] ?? SERVICE_URLS.auth;
  return `${base}${path}`;
}

let refreshPromise = null;

export class ApiError extends Error {
  constructor(status, errorCode, message, details = []) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.errorCode = errorCode;
    this.details = details;
  }
}

function notifySessionExpired() {
  clearSession();
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new Event('healthcare:session-expired'));
  }
}

async function parseResponse(response) {
  if (response.status === 204) {
    return { success: true, data: null };
  }

  try {
    return await response.json();
  } catch {
    if (!response.ok) {
      throw new ApiError(response.status, 'NETWORK_ERROR', `HTTP ${response.status}`);
    }
    return { success: true, data: null };
  }
}

async function refreshAccessToken() {
  const session = loadSession();
  if (!session?.refreshToken || session.refreshToken === 'mock-refresh-token') {
    return null;
  }

  if (!refreshPromise) {
    refreshPromise = fetch(buildUrl('auth', '/api/v1/auth/refresh'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: session.refreshToken }),
    })
      .then(async (response) => {
        const body = await parseResponse(response);
        if (!response.ok || body.success === false || !body.data?.accessToken) {
          throw new ApiError(
            response.status,
            body.errorCode ?? 'UNAUTHORIZED',
            body.message ?? 'Session expired',
            body.details ?? [],
          );
        }
        updateAccessToken(body.data.accessToken);
        return body.data.accessToken;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
}

async function send(service, path, fetchOptions, headers) {
  return fetch(buildUrl(service, path), {
    ...fetchOptions,
    headers,
  });
}

export async function httpClient(service, path, options = {}) {
  const { skipAuth = false, retryOnUnauthorized = true, ...fetchOptions } = options;

  const headers = {
    'Content-Type': 'application/json',
    ...(fetchOptions.headers ?? {}),
  };

  if (!skipAuth) {
    const token = loadSession()?.accessToken;
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  let response = await send(service, path, fetchOptions, headers);

  if (response.status === 401 && !skipAuth && retryOnUnauthorized && service !== 'auth') {
    try {
      const newToken = await refreshAccessToken();
      if (newToken) {
        headers.Authorization = `Bearer ${newToken}`;
        response = await send(service, path, fetchOptions, headers);
      }
    } catch {
      notifySessionExpired();
    }
  }

  const body = await parseResponse(response);

  if (!response.ok || body.success === false) {
    if (response.status === 401 && !skipAuth) {
      notifySessionExpired();
    }
    throw new ApiError(
      response.status,
      body.errorCode ?? 'UNKNOWN_ERROR',
      body.message ?? `Request failed with status ${response.status}`,
      body.details ?? [],
    );
  }

  return body;
}

export const authApi = (path, options) => httpClient('auth', path, options);
export const userApi = (path, options) => httpClient('user', path, options);
export const doctorApi = (path, options) => httpClient('doctor', path, options);
export const appointmentApi = (path, options) => httpClient('appointment', path, options);
export const notificationApi = (path, options) => httpClient('notification', path, options);
