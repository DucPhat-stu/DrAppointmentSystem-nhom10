import { useNavigate } from 'react-router-dom';
import PagePlaceholder from './PagePlaceholder.jsx';
import { useAuth } from '../hooks/useAuth.js';

export default function LoginPage() {
  const navigate = useNavigate();
  const { setSession } = useAuth();

  const handleLogin = () => {
    setSession({
      email: 'patient01@healthcare.local',
      role: 'PATIENT',
    });
    navigate('/doctors');
  };

  return (
    <div style={{ padding: '1.5rem' }}>
      <PagePlaceholder
        eyebrow="Route Ready"
        title="Login flow scaffolded for patient booking."
        description="Phase 0 keeps authentication lightweight. This screen seeds a local session so protected routes, API wrappers and route guards are already wired for later integration."
      />
      <button onClick={handleLogin} style={{ marginTop: '1rem' }} type="button">
        Enter MVP Shell
      </button>
    </div>
  );
}

