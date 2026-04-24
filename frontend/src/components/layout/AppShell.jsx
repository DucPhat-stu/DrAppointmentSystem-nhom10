import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';
import styles from './AppShell.module.css';

const navItems = [
  { to: '/doctors', label: 'Doctors' },
  { to: '/appointments/book', label: 'Booking' },
  { to: '/profile', label: 'Profile' },
  { to: '/notifications', label: 'Notifications' },
];

export default function AppShell({ children }) {
  const { session, setSession } = useAuth();

  return (
    <div className={styles.shell}>
      <header className={styles.header}>
        <Link className={styles.brand} to="/doctors">
          <span className={styles.brandIcon}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
            </svg>
          </span>
          HealthCare
        </Link>
        <nav className={styles.nav}>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              className={({ isActive }) => (isActive ? styles.activeLink : styles.link)}
              to={item.to}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <button className={styles.logoutButton} onClick={() => setSession(null)} type="button">
          Logout
        </button>
      </header>
      <main className={styles.main}>{children}</main>
    </div>
  );
}
