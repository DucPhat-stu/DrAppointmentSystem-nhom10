import styles from './AuthLayout.module.css';
import BrandingPanel from './BrandingPanel.jsx';

export default function AuthLayout({ children, title, subtitle }) {
  return (
    <div className={styles.layout}>
      {/* Animated background orbs */}
      <div className={styles.orbContainer} aria-hidden="true">
        <div className={styles.orb1} />
        <div className={styles.orb2} />
        <div className={styles.orb3} />
      </div>

      {/* Left: Branding */}
      <aside className={styles.brandingSide}>
        <BrandingPanel />
      </aside>

      {/* Right: Form */}
      <main className={styles.formSide}>
        <div className={styles.formContainer}>
          {/* Header */}
          <div className={styles.formHeader}>
            {/* Mobile Logo */}
            <div className={styles.mobileLogo}>
              <span className={styles.logoIcon}>
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
                </svg>
              </span>
              <span className={styles.logoText}>HealthCare</span>
            </div>

            <h1 className={styles.title}>{title}</h1>
            {subtitle && <p className={styles.subtitle}>{subtitle}</p>}
          </div>

          {/* Form Content */}
          <div className={styles.formContent}>
            {children}
          </div>
        </div>
      </main>
    </div>
  );
}
