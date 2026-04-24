/* =========================================================
   Auth Context & Provider – Enhanced with login/logout/register actions
   ========================================================= */

import { createContext, useCallback, useEffect, useState } from 'react';
import { clearSession, loadSession, persistSession } from '../services/sessionStorageService.js';
import * as authService from '../services/authService.js';
import { ApiError } from '../services/httpClient.js';

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

  /**
   * Login action – calls auth-service API, sets session.
   * Falls back to mock if API is unavailable.
   */
  const loginAction = useCallback(async ({ email, password, actor = 'PATIENT' }) => {
    try {
      const response = await authService.login({ email, password, actor });

      const sessionData = {
        email: response.data?.email ?? email,
        role: response.data?.role ?? actor,
        accessToken: response.data?.accessToken ?? null,
        refreshToken: response.data?.refreshToken ?? null,
        fullName: response.data?.fullName ?? email.split('@')[0],
      };

      setSession(sessionData);
      return { success: true };
    } catch (err) {
      // If backend unavailable, allow mock login for development
      if (!(err instanceof ApiError)) {
        console.warn('[Auth] Backend unreachable, using mock session for dev.');
        setSession({
          email,
          role: actor,
          accessToken: 'mock-dev-token',
          refreshToken: 'mock-refresh-token',
          fullName: email.split('@')[0],
        });
        return { success: true, mock: true };
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
      if (!(err instanceof ApiError)) {
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
