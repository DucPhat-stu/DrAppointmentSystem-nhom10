/* =========================================================
   HTTP Client – Auto-attach JWT, parse API envelope, handle errors
   ========================================================= */

const SERVICE_URLS = {
  auth: import.meta.env.VITE_AUTH_URL ?? 'http://localhost:8086',
  user: import.meta.env.VITE_USER_URL ?? 'http://localhost:8082',
  doctor: import.meta.env.VITE_DOCTOR_URL ?? 'http://localhost:8083',
  appointment: import.meta.env.VITE_APPOINTMENT_URL ?? 'http://localhost:8084',
  notification: import.meta.env.VITE_NOTIFICATION_URL ?? 'http://localhost:8085',
};

/**
 * Build full URL from service name + path.
 * Example: buildUrl('auth', '/api/v1/auth/login')
 */
function buildUrl(service, path) {
  const base = SERVICE_URLS[service] ?? SERVICE_URLS.auth;
  return `${base}${path}`;
}

/**
 * Get current access token from storage.
 */
function getAccessToken() {
  try {
    const raw = sessionStorage.getItem('healthcare.session');
    if (!raw) return null;
    const session = JSON.parse(raw);
    return session?.accessToken ?? null;
  } catch {
    return null;
  }
}

function getSession() {
  try {
    const raw = sessionStorage.getItem('healthcare.session');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

/**
 * Custom API error with structured data from backend envelope.
 */
export class ApiError extends Error {
  constructor(status, errorCode, message, details = []) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.errorCode = errorCode;
    this.details = details;
  }
}

/**
 * Main HTTP client.
 *
 * @param {string} service - Service name: 'auth' | 'user' | 'doctor' | 'appointment' | 'notification'
 * @param {string} path - API path, e.g. '/api/v1/auth/login'
 * @param {object} options - fetch options (method, body, headers, etc.)
 * @param {boolean} options.skipAuth - skip auto-attaching JWT
 * @returns {Promise<object>} - parsed response body (envelope data, or full envelope)
 */
export async function httpClient(service, path, options = {}) {
  const { skipAuth = false, ...fetchOptions } = options;

  const headers = {
    'Content-Type': 'application/json',
    ...(fetchOptions.headers ?? {}),
  };

  // Auto-attach JWT unless skipped
  if (!skipAuth) {
    const session = getSession();
    const token = session?.accessToken ?? getAccessToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
  }

  const url = buildUrl(service, path);

  const response = await fetch(url, {
    ...fetchOptions,
    headers,
  });

  // Handle empty response (204 No Content)
  if (response.status === 204) {
    return { success: true, data: null };
  }

  let body;
  try {
    body = await response.json();
  } catch {
    if (!response.ok) {
      throw new ApiError(response.status, 'NETWORK_ERROR', `HTTP ${response.status}`);
    }
    return { success: true, data: null };
  }

  // Handle API error envelope
  if (!response.ok || body.success === false) {
    throw new ApiError(
      response.status,
      body.errorCode ?? 'UNKNOWN_ERROR',
      body.message ?? `Request failed with status ${response.status}`,
      body.details ?? [],
    );
  }

  return body;
}

/**
 * Shorthand helpers per service.
 */
export const authApi = (path, options) => httpClient('auth', path, options);
export const userApi = (path, options) => httpClient('user', path, options);
export const doctorApi = (path, options) => httpClient('doctor', path, options);
export const appointmentApi = (path, options) => httpClient('appointment', path, options);
export const notificationApi = (path, options) => httpClient('notification', path, options);
