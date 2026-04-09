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
          Healthcare Platform
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
          {session?.email ?? 'Logout'}
        </button>
      </header>
      <main className={styles.main}>{children}</main>
    </div>
  );
}

