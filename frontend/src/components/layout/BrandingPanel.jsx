import styles from './BrandingPanel.module.css';
import brandingImage from '../../assets/medical-branding.png';

export default function BrandingPanel() {
  return (
    <div className={styles.panel}>
      {/* Background gradient layer */}
      <div className={styles.gradientOverlay} />

      {/* Content */}
      <div className={styles.content}>
        {/* Logo */}
        <div className={styles.logo}>
          <span className={styles.logoIcon}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
            </svg>
          </span>
          <span className={styles.logoText}>HealthCare</span>
        </div>

        {/* Tagline */}
        <div className={styles.tagline}>
          <h2 className={styles.taglineTitle}>
            Your Health,{' '}
            <span className={styles.highlight}>Our Priority</span>
          </h2>
          <p className={styles.taglineSubtitle}>
            Modern healthcare platform connecting patients with trusted doctors.
            Book appointments seamlessly, manage your health records, and stay
            informed — all in one secure place.
          </p>
        </div>

        {/* Illustration */}
        <div className={styles.illustrationWrapper}>
          <img
            className={styles.illustration}
            src={brandingImage}
            alt="Healthcare platform illustration"
            loading="eager"
          />
        </div>

        {/* Feature indicators */}
        <div className={styles.features}>
          <div className={styles.featureItem}>
            <span className={styles.featureDot} />
            <span>Secure & Private</span>
          </div>
          <div className={styles.featureItem}>
            <span className={styles.featureDot} />
            <span>24/7 Available</span>
          </div>
          <div className={styles.featureItem}>
            <span className={styles.featureDot} />
            <span>Trusted Doctors</span>
          </div>
        </div>
      </div>
    </div>
  );
}
