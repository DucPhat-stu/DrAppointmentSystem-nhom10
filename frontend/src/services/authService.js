/* =========================================================
   Auth Service – API calls to auth-service (port 8086)
   Endpoints: register, login, refresh, logout
   ========================================================= */

import { authApi } from './httpClient.js';

/**
 * Register a new patient account.
 * POST /api/v1/auth/register
 */
export async function register({ firstName, lastName, email, password }) {
  const response = await authApi('/api/v1/auth/register', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify({
      fullName: `${firstName} ${lastName}`.trim(),
      email,
      password,
    }),
  });
  return response;
}

/**
 * Login with email/password.
 * POST /api/v1/auth/login
 * @param {object} payload - { email, password, actor? }
 * @returns {{ data: { accessToken, refreshToken, email, role } }}
 */
export async function login({ email, password, actor }) {
  const body = { email, password };
  if (actor) {
    body.actor = actor;
  }

  const response = await authApi('/api/v1/auth/login', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify(body),
  });
  return response;
}

/**
 * Refresh access token.
 * POST /api/v1/auth/refresh
 * @param {string} refreshToken
 */
export async function refreshToken(refreshTokenValue) {
  const response = await authApi('/api/v1/auth/refresh', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify({ refreshToken: refreshTokenValue }),
  });
  return response;
}

/**
 * Logout – revoke refresh token.
 * POST /api/v1/auth/logout
 * @param {string} refreshToken
 */
export async function logout(refreshTokenValue) {
  const response = await authApi('/api/v1/auth/logout', {
    method: 'POST',
    body: JSON.stringify({ refreshToken: refreshTokenValue }),
  });
  return response;
}
