const SESSION_KEY = 'healthcare.session';

function getStorage() {
  if (typeof window === 'undefined') {
    return null;
  }

  return window.sessionStorage;
}

export function loadSession() {
  const storage = getStorage();
  const rawSession = storage?.getItem(SESSION_KEY);
  return rawSession ? JSON.parse(rawSession) : null;
}

export function persistSession(session) {
  const storage = getStorage();
  if (!storage) {
    return;
  }

  storage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearSession() {
  const storage = getStorage();
  storage?.removeItem(SESSION_KEY);
}

