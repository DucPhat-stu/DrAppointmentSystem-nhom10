/* =========================================================
   Auth Context & Provider – Enhanced with login/logout/register actions
   ========================================================= */

import { createContext, useCallback, useEffect, useState } from 'react';
import { clearSession, loadSession, persistSession } from '../services/sessionStorageService.js';
import * as authService from '../services/authService.js';
import { ApiError } from '../services/httpClient.js';

const allowMockAuthFallback = import.meta.env.DEV;

export const AuthContext = createContext({
  session: null,
  setSession: () => {},
  loginAction: async () => {},
  registerAction: async () => {},
  logoutAction: async () => {},
  isAuthenticated: false,
});

export function AppProviders({ children }) {
  const [session, setSession] = useState(() => loadSession());

  // Sync to storage
  useEffect(() => {
    if (session) {
      persistSession(session);
    } else {
      clearSession();
    }
  }, [session]);

  useEffect(() => {
    function handleSessionExpired() {
      setSession(null);
    }
    function handleSessionUpdated(event) {
      setSession(event.detail ?? loadSession());
    }

    window.addEventListener('healthcare:session-expired', handleSessionExpired);
    window.addEventListener('healthcare:session-updated', handleSessionUpdated);
    return () => {
      window.removeEventListener('healthcare:session-expired', handleSessionExpired);
      window.removeEventListener('healthcare:session-updated', handleSessionUpdated);
    };
  }, []);

  /**
   * Login action – calls auth-service API, sets session.
   * Falls back to mock if API is unavailable.
   */
  const loginAction = useCallback(async ({ email, password, actor }) => {
    try {
      const response = await authService.login({ email, password, actor });

      const sessionData = {
        userId: response.data?.userId ?? null,
        email: response.data?.email ?? email,
        role: response.data?.role ?? actor ?? 'PATIENT',
        accessToken: response.data?.accessToken ?? null,
        refreshToken: response.data?.refreshToken ?? null,
        fullName: response.data?.fullName ?? email.split('@')[0],
      };

      setSession(sessionData);
      return { success: true, session: sessionData };
    } catch (err) {
      // If backend is unavailable, allow mock auth only during local development.
      if (allowMockAuthFallback && !(err instanceof ApiError)) {
        console.warn('[Auth] Backend unreachable, using mock session for dev.');
        const mockSession = {
          email,
          role: actor ?? 'PATIENT',
          accessToken: 'mock-dev-token',
          refreshToken: 'mock-refresh-token',
          fullName: email.split('@')[0],
        };
        setSession(mockSession);
        return { success: true, mock: true, session: mockSession };
      }
      throw err;
    }
  }, []);

  /**
   * Register action – calls auth-service API.
   * Falls back to mock if API is unavailable.
   */
  const registerAction = useCallback(async ({ firstName, lastName, email, password }) => {
    try {
      await authService.register({ firstName, lastName, email, password });
      return { success: true };
    } catch (err) {
      if (allowMockAuthFallback && !(err instanceof ApiError)) {
        console.warn('[Auth] Backend unreachable, mock register success for dev.');
        return { success: true, mock: true };
      }
      throw err;
    }
  }, []);

  /**
   * Logout action – calls auth-service API to revoke token, clears session.
   */
  const logoutAction = useCallback(async () => {
    try {
      if (session?.refreshToken && session.refreshToken !== 'mock-refresh-token') {
        await authService.logout(session.refreshToken);
      }
    } catch (err) {
      console.warn('[Auth] Logout API failed, clearing session anyway.', err);
    }
    setSession(null);
  }, [session]);

  const contextValue = {
    session,
    setSession,
    loginAction,
    registerAction,
    logoutAction,
    isAuthenticated: !!session,
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}
