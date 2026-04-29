import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';
import styles from './Navbar.module.css';

const publicLinks = [
  { to: '#services', label: 'Services' },
  { to: '#about', label: 'About' },
  { to: '#contact', label: 'Contact' },
];

const authLinks = [
  { to: '/doctors', label: 'Doctors' },
  { to: '/appointments/book', label: 'Book Now' },
  { to: '/chat', label: 'AI Chat' },
  { to: '/notifications', label: 'Notifications' },
];

export default function Navbar() {
  const { isAuthenticated, logoutAction, session } = useAuth();
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Close mobile menu on resize
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth > 768) setMobileOpen(false);
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const navLinks = isAuthenticated ? authLinks : publicLinks;

  const handleAnchorClick = (e, to) => {
    if (to.startsWith('#')) {
      e.preventDefault();
      const el = document.querySelector(to);
      if (el) {
        el.scrollIntoView({ behavior: 'smooth' });
      }
      setMobileOpen(false);
    }
  };

  return (
    <header className={`${styles.navbar} ${scrolled ? styles.scrolled : ''}`}>
      <div className={styles.container}>
        {/* Brand */}
        <Link to="/" className={styles.brand}>
          <span className={styles.brandIcon}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
            </svg>
          </span>
          <span className={styles.brandText}>HealthCare</span>
        </Link>

        {/* Desktop Nav */}
        <nav className={styles.desktopNav}>
          {navLinks.map((link) => (
            link.to.startsWith('#') ? (
              <a
                key={link.to}
                href={link.to}
                className={styles.navLink}
                onClick={(e) => handleAnchorClick(e, link.to)}
              >
                {link.label}
              </a>
            ) : (
              <Link key={link.to} to={link.to} className={styles.navLink}>
                {link.label}
              </Link>
            )
          ))}
        </nav>

        {/* Auth Actions */}
        <div className={styles.actions}>
          {isAuthenticated ? (
            <>
              <Link to="/profile" className={styles.profileBtn}>
                <span className={styles.avatar}>
                  {(session?.fullName?.[0] || session?.email?.[0] || 'U').toUpperCase()}
                </span>
                <span className={styles.profileName}>
                  {session?.fullName || session?.email?.split('@')[0]}
                </span>
              </Link>
              <button
                type="button"
                className={styles.logoutBtn}
                onClick={logoutAction}
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className={styles.loginBtn}>Sign In</Link>
              <Link to="/register" className={styles.registerBtn}>Get Started</Link>
            </>
          )}
        </div>

        {/* Mobile Hamburger */}
        <button
          type="button"
          className={`${styles.hamburger} ${mobileOpen ? styles.hamburgerOpen : ''}`}
          onClick={() => setMobileOpen((p) => !p)}
          aria-label="Toggle menu"
        >
          <span />
          <span />
          <span />
        </button>
      </div>

      {/* Mobile Menu */}
      {mobileOpen && (
        <div className={styles.mobileMenu}>
          <nav className={styles.mobileNav}>
            {navLinks.map((link) => (
              link.to.startsWith('#') ? (
                <a
                  key={link.to}
                  href={link.to}
                  className={styles.mobileLink}
                  onClick={(e) => {
                    handleAnchorClick(e, link.to);
                    setMobileOpen(false);
                  }}
                >
                  {link.label}
                </a>
              ) : (
                <Link
                  key={link.to}
                  to={link.to}
                  className={styles.mobileLink}
                  onClick={() => setMobileOpen(false)}
                >
                  {link.label}
                </Link>
              )
            ))}
          </nav>
          <div className={styles.mobileActions}>
            {isAuthenticated ? (
              <button type="button" className={styles.logoutBtn} onClick={logoutAction}>
                Logout
              </button>
            ) : (
              <>
                <Link to="/login" className={styles.loginBtn} onClick={() => setMobileOpen(false)}>
                  Sign In
                </Link>
                <Link to="/register" className={styles.registerBtn} onClick={() => setMobileOpen(false)}>
                  Get Started
                </Link>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
