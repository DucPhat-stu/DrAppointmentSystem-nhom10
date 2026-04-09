import { createContext, useEffect, useState } from 'react';

export const AuthContext = createContext({
  session: null,
  setSession: () => {},
});

const SESSION_KEY = 'healthcare.session';

export function AppProviders({ children }) {
  const [session, setSession] = useState(() => {
    const rawSession = window.sessionStorage.getItem(SESSION_KEY);
    return rawSession ? JSON.parse(rawSession) : null;
  });

  useEffect(() => {
    if (session) {
      window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
      return;
    }

    window.sessionStorage.removeItem(SESSION_KEY);
  }, [session]);

  return (
    <AuthContext.Provider value={{ session, setSession }}>
      {children}
    </AuthContext.Provider>
  );
}

