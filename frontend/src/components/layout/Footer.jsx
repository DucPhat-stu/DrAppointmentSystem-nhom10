import { useScrollReveal } from '../../hooks/useScrollReveal.js';
import styles from './Footer.module.css';

export default function Footer() {
  const currentYear = new Date().getFullYear();
  const { ref, isVisible } = useScrollReveal({ threshold: 0.05 });

  return (
    <footer ref={ref} className={`${styles.footer} ${isVisible ? styles.visible : ''}`}>
      <div className={styles.container}>
        {/* Brand Column */}
        <div className={styles.brandCol}>
          <div className={styles.brand}>
            <span className={styles.brandIcon}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
              </svg>
            </span>
            <span className={styles.brandText}>HealthCare</span>
          </div>
          <p className={styles.brandDesc}>
            Nền tảng y tế hiện đại kết nối bệnh nhân với bác sĩ uy tín.
            Bảo mật, đáng tin cậy và dễ sử dụng.
          </p>

          {/* Social Icons */}
          <div className={styles.socials}>
            {/* Facebook */}
            <a
              href="https://facebook.com/healthcare"
              target="_blank"
              rel="noopener noreferrer"
              className={styles.socialLink}
              aria-label="Facebook"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
              </svg>
            </a>

            {/* Zalo */}
            <a
              href="https://zalo.me/healthcare"
              target="_blank"
              rel="noopener noreferrer"
              className={styles.socialLink}
              aria-label="Zalo"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.04 2 11c0 2.76 1.36 5.22 3.48 6.87V22l3.6-2c.93.26 1.92.4 2.92.4 5.52 0 10-4.04 10-9S17.52 2 12 2zm1.13 12.22H8.58c-.31 0-.56-.25-.56-.56s.25-.56.56-.56h4.55c.31 0 .56.25.56.56s-.25.56-.56.56zm2.11-3H8.58c-.31 0-.56-.25-.56-.56s.25-.56.56-.56h6.66c.31 0 .56.25.56.56s-.25.56-.56.56zm0-3H8.58c-.31 0-.56-.25-.56-.56s.25-.56.56-.56h6.66c.31 0 .56.25.56.56s-.25.56-.56.56z"/>
              </svg>
            </a>

            {/* LinkedIn */}
            <a
              href="https://linkedin.com/company/healthcare"
              target="_blank"
              rel="noopener noreferrer"
              className={styles.socialLink}
              aria-label="LinkedIn"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433a2.062 2.062 0 0 1-2.063-2.065 2.064 2.064 0 1 1 2.063 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/>
              </svg>
            </a>

            {/* GitHub */}
            <a
              href="https://github.com/healthcare"
              target="_blank"
              rel="noopener noreferrer"
              className={styles.socialLink}
              aria-label="GitHub"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
              </svg>
            </a>
          </div>
        </div>

        {/* Links Columns */}
        <div className={styles.linksCol}>
          <h4 className={styles.colTitle}>Nền tảng</h4>
          <ul className={styles.linkList}>
            <li><a href="#services">Dịch vụ</a></li>
            <li><a href="#how-it-works">Quy trình</a></li>
            <li><a href="#about">Về chúng tôi</a></li>
            <li><a href="#contact">Liên hệ</a></li>
          </ul>
        </div>

        <div className={styles.linksCol}>
          <h4 className={styles.colTitle}>Hỗ trợ</h4>
          <ul className={styles.linkList}>
            <li><a href="#">Trung tâm hỗ trợ</a></li>
            <li><a href="#">Chính sách bảo mật</a></li>
            <li><a href="#">Điều khoản sử dụng</a></li>
          </ul>
        </div>

        <div className={styles.linksCol}>
          <h4 className={styles.colTitle}>Liên hệ</h4>
          <ul className={styles.linkList}>
            <li className={styles.contactItem}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                <polyline points="22,6 12,13 2,6"/>
              </svg>
              support@healthcare.io
            </li>
            <li className={styles.contactItem}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72"/>
              </svg>
              +84 (0) 123 456 789
            </li>
          </ul>
        </div>
      </div>

      {/* Bottom Bar */}
      <div className={styles.bottom}>
        <p>© {currentYear} HealthCare Platform. All rights reserved.</p>
      </div>
    </footer>
  );
}
