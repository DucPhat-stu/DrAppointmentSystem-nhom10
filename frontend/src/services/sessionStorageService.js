/* =========================================================
   Session Storage Service – Token + session persistence
   ========================================================= */

const SESSION_KEY = 'healthcare.session';

function getStorage() {
  if (typeof window === 'undefined') return null;
  return window.sessionStorage;
}

/**
 * Load session from storage.
 * @returns {{ email, role, accessToken, refreshToken, fullName } | null}
 */
export function loadSession() {
  const storage = getStorage();
  const raw = storage?.getItem(SESSION_KEY);
  if (!raw) return null;

  try {
    return JSON.parse(raw);
  } catch {
    storage?.removeItem(SESSION_KEY);
    return null;
  }
}

/**
 * Save session to storage.
 */
export function persistSession(session) {
  const storage = getStorage();
  if (!storage || !session) return;
  storage.setItem(SESSION_KEY, JSON.stringify(session));
}

/**
 * Clear session from storage.
 */
export function clearSession() {
  const storage = getStorage();
  storage?.removeItem(SESSION_KEY);
}

/**
 * Update just the access token (after refresh).
 */
export function updateAccessToken(newAccessToken) {
  const session = loadSession();
  if (!session) return;
  session.accessToken = newAccessToken;
  persistSession(session);
}
