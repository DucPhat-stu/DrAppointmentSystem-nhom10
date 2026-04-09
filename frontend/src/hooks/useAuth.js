import { useContext } from 'react';
import { AuthContext } from '../app/providers.jsx';

export function useAuth() {
  return useContext(AuthContext);
}

