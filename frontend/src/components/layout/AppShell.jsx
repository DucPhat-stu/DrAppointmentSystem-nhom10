import { Link, NavLink, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';
import styles from './AppShell.module.css';

const navItems = [
  {
    to: '/doctors',
    label: 'Doctors',
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
        <circle cx="12" cy="7" r="4"/>
      </svg>
    ),
  },
  {
    to: '/doctor/schedules',
    label: 'Schedules',
    doctorOnly: true,
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
        <line x1="16" y1="2" x2="16" y2="6"/>
        <line x1="8" y1="2" x2="8" y2="6"/>
        <line x1="3" y1="10" x2="21" y2="10"/>
      </svg>
    ),
  },
  {
    to: '/doctor/appointments?status=PENDING',
    label: 'Appointment requests',
    doctorOnly: true,
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2"/>
        <path d="M3 10h18"/>
        <path d="m9 16 2 2 4-4"/>
      </svg>
    ),
  },
  {
    to: '/doctor/leaves',
    label: 'Leaves',
    doctorOnly: true,
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2"/>
        <path d="M3 10h18"/>
        <path d="M8 15h8"/>
      </svg>
    ),
  },
  {
    to: '/admin',
    label: 'Dashboard',
    adminOnly: true,
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="3" width="7" height="7" rx="1"/>
        <rect x="14" y="3" width="7" height="7" rx="1"/>
        <rect x="14" y="14" width="7" height="7" rx="1"/>
        <rect x="3" y="14" width="7" height="7" rx="1"/>
      </svg>
    ),
  },
  {
    to: '/admin/leaves',
    label: 'Leave approvals',
    adminOnly: true,
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2"/>
        <path d="M3 10h18"/>
        <path d="m9 16 2 2 4-4"/>
      </svg>
    ),
  },
  {
    to: '/appointments/book',
    label: 'Book appointment',
    patientOnly: true,
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
        <line x1="16" y1="2" x2="16" y2="6"/>
        <line x1="8" y1="2" x2="8" y2="6"/>
        <line x1="3" y1="10" x2="21" y2="10"/>
      </svg>
    ),
  },
  {
    to: '/appointments',
    label: 'My appointments',
    patientOnly: true,
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2"/>
        <path d="M3 10h18"/>
        <path d="M8 14h8"/>
        <path d="M8 18h5"/>
      </svg>
    ),
  },
  {
    to: '/doctor/appointments?status=CONFIRMED',
    label: 'Appointments',
    doctorOnly: true,
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2"/>
        <path d="M3 10h18"/>
        <path d="M8 14h8"/>
        <path d="M8 18h5"/>
      </svg>
    ),
  },
  {
    to: '/profile',
    label: 'Profile',
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
        <polyline points="14 2 14 8 20 8"/>
        <line x1="16" y1="13" x2="8" y2="13"/>
        <line x1="16" y1="17" x2="8" y2="17"/>
      </svg>
    ),
  },
  {
    to: '/notifications',
    label: 'Notifications',
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      </svg>
    ),
  },
];

export default function AppShell({ children }) {
  const { session, logoutAction } = useAuth();
  const role = session?.role?.replace(/^ROLE_/, '').toUpperCase();
  const location = useLocation();
  const currentPath = `${location.pathname}${location.search}`;

  return (
    <div className={styles.shell}>
      <aside className={styles.sidebar}>
        <div className={styles.sidebarTop}>
          <Link className={styles.brand} to="/">
            <span className={styles.brandIcon}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
              </svg>
            </span>
            <span className={styles.brandText}>HealthCare</span>
          </Link>

          <nav className={styles.nav}>
            {navItems
              .filter((item) => !item.doctorOnly || role === 'DOCTOR')
              .filter((item) => !item.patientOnly || role === 'PATIENT')
              .filter((item) => !item.adminOnly || role === 'ADMIN' || role === 'SUPER_ADMIN')
              .map((item) => (
                <NavLink
                  key={item.to}
                  className={({ isActive }) => {
                    const active = item.to.includes('?') ? currentPath === item.to : isActive;
                    return `${styles.navLink} ${active ? styles.active : ''}`;
                  }}
                  to={item.to}
                >
                  <span className={styles.navIcon}>{item.icon}</span>
                  <span className={styles.navLabel}>{item.label}</span>
                </NavLink>
              ))}
          </nav>
        </div>

        <div className={styles.sidebarBottom}>
          {session && (
            <div className={styles.userCard}>
              <div className={styles.avatar}>
                {session.fullName?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              <div className={styles.userInfo}>
                <span className={styles.userName}>{session.fullName || 'User'}</span>
                <span className={styles.userRole}>{session.role || 'Patient'}</span>
              </div>
            </div>
          )}
          <button className={styles.logoutButton} onClick={logoutAction} type="button">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            Sign out
          </button>
        </div>
      </aside>

      <main className={styles.main}>{children}</main>
    </div>
  );
}
