import { httpClient } from './httpClient.js';

export function login(payload) {
  return httpClient('/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function refreshToken(payload) {
  return httpClient('/auth/refresh', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

