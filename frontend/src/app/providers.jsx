import { createContext, useEffect, useState } from 'react';
import { clearSession, loadSession, persistSession } from '../services/sessionStorageService.js';

export const AuthContext = createContext({
  session: null,
  setSession: () => {},
});

export function AppProviders({ children }) {
  const [session, setSession] = useState(() => loadSession());

  useEffect(() => {
    if (session) {
      persistSession(session);
      return;
    }

    clearSession();
  }, [session]);

  return (
    <AuthContext.Provider value={{ session, setSession }}>
      {children}
    </AuthContext.Provider>
  );
}
