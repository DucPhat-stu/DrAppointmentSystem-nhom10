import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';
import styles from './HeroSection.module.css';

export default function HeroSection() {
  const { isAuthenticated } = useAuth();

  return (
    <section className={styles.hero} id="hero">
      {/* Animated background */}
      <div className={styles.bg} aria-hidden="true">
        <div className={styles.orb1} />
        <div className={styles.orb2} />
        <div className={styles.orb3} />
        <div className={styles.grid} />
      </div>

      <div className={styles.container}>
        <div className={styles.content}>
          {/* Badge */}
          <div className={styles.badge}>
            <span className={styles.badgeDot} />
            <span>Trusted by 10,000+ patients</span>
          </div>

          {/* Title */}
          <h1 className={styles.title}>
            Modern Healthcare{' '}
            <span className={styles.highlight}>at Your Fingertips</span>
          </h1>

          {/* Subtitle */}
          <p className={styles.subtitle}>
            Book appointments with top-rated doctors, manage your health records,
            and receive real-time notifications — all in one secure, easy-to-use platform.
          </p>

          {/* CTA Buttons */}
          <div className={styles.ctas}>
            {isAuthenticated ? (
              <Link to="/doctors" className={styles.ctaPrimary}>
                Browse Doctors
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="5" y1="12" x2="19" y2="12"/>
                  <polyline points="12 5 19 12 12 19"/>
                </svg>
              </Link>
            ) : (
              <>
                <Link to="/register" className={styles.ctaPrimary}>
                  Get Started Free
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <line x1="5" y1="12" x2="19" y2="12"/>
                    <polyline points="12 5 19 12 12 19"/>
                  </svg>
                </Link>
                <Link to="/login" className={styles.ctaSecondary}>
                  Sign In
                </Link>
              </>
            )}
          </div>

          {/* Stats */}
          <div className={styles.stats}>
            <div className={styles.stat}>
              <span className={styles.statValue}>500+</span>
              <span className={styles.statLabel}>Doctors</span>
            </div>
            <div className={styles.statDivider} />
            <div className={styles.stat}>
              <span className={styles.statValue}>50k+</span>
              <span className={styles.statLabel}>Appointments</span>
            </div>
            <div className={styles.statDivider} />
            <div className={styles.stat}>
              <span className={styles.statValue}>4.9</span>
              <span className={styles.statLabel}>Rating</span>
            </div>
          </div>
        </div>

        {/* Right Side – Feature Cards */}
        <div className={styles.visual}>
          <div className={styles.featureGrid}>
            <div className={`${styles.featureCard} ${styles.card1}`}>
              <div className={styles.cardIcon}>
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
                </svg>
              </div>
              <h3>Health Monitoring</h3>
              <p>Track your vitals and health metrics in real-time</p>
            </div>

            <div className={`${styles.featureCard} ${styles.card2}`}>
              <div className={styles.cardIcon}>
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </div>
              <h3>Easy Booking</h3>
              <p>Schedule appointments with just a few clicks</p>
            </div>

            <div className={`${styles.featureCard} ${styles.card3}`}>
              <div className={styles.cardIcon}>
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                </svg>
              </div>
              <h3>Secure & Private</h3>
              <p>Your data is encrypted and HIPAA compliant</p>
            </div>

            <div className={`${styles.featureCard} ${styles.card4}`}>
              <div className={styles.cardIcon}>
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                  <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                </svg>
              </div>
              <h3>Notifications</h3>
              <p>Never miss an appointment with smart reminders</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
