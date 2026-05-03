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

export async function forgotPassword(email) {
  return authApi('/api/v1/auth/forgot-password', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify({ email }),
  });
}

export async function resetPassword({ token, newPassword }) {
  return authApi('/api/v1/auth/reset-password', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify({ token, newPassword }),
  });
}

export async function changePassword({ currentPassword, newPassword }) {
  return authApi('/api/v1/auth/change-password', {
    method: 'POST',
    body: JSON.stringify({ currentPassword, newPassword }),
  });
}

export async function requestOtp(phone) {
  return authApi('/api/v1/auth/otp/request', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify({ phone }),
  });
}

export async function verifyOtp({ phone, otp }) {
  return authApi('/api/v1/auth/otp/verify', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify({ phone, otp }),
  });
}

export async function loginByDoctorCode(doctorCode) {
  return authApi('/api/v1/auth/doctor-code/login', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify({ doctorCode }),
  });
}

export async function setupTwoFactor() {
  return authApi('/api/v1/auth/2fa/setup', {
    method: 'POST',
  });
}

export async function verifyTwoFactor(code) {
  return authApi('/api/v1/auth/2fa/verify', {
    method: 'POST',
    body: JSON.stringify({ code }),
  });
}

export async function twoFactorLogin({ email, password, code }) {
  return authApi('/api/v1/auth/2fa/login', {
    method: 'POST',
    skipAuth: true,
    body: JSON.stringify({ email, password, code }),
  });
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
