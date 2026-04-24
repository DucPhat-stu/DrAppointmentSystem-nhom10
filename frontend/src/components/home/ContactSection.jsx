import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';
import { useScrollReveal } from '../../hooks/useScrollReveal.js';
import styles from './ContactSection.module.css';

export default function ContactSection() {
  const { isAuthenticated } = useAuth();
  const { ref, isVisible } = useScrollReveal({ threshold: 0.15 });

  return (
    <section
      ref={ref}
      className={`${styles.section} ${isVisible ? styles.visible : ''}`}
      id="contact"
    >
      <div className={styles.container}>
        {/* Header */}
        <div className={styles.header}>
          <span className={styles.badge}>Liên Hệ</span>
          <h2 className={styles.title}>
            Sẵn Sàng <span className={styles.highlight}>Bắt Đầu</span>?
          </h2>
          <p className={styles.subtitle}>
            Tham gia cùng hàng ngàn bệnh nhân đã tin tưởng HealthCare Platform
          </p>
        </div>

        {/* CTA Banner */}
        <div className={styles.banner}>
          {/* Decorative orbs */}
          <div className={styles.bannerOrb1} aria-hidden="true" />
          <div className={styles.bannerOrb2} aria-hidden="true" />

          <div className={styles.bannerContent}>
            <h3>Bắt đầu hành trình sức khỏe ngay hôm nay</h3>
            <p>
              Tạo tài khoản miễn phí và đặt lịch hẹn khám bệnh đầu tiên chỉ trong vài phút.
            </p>
          </div>

          {isAuthenticated ? (
            <Link to="/doctors" className={styles.bannerBtn}>
              Tìm Bác Sĩ
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <line x1="5" y1="12" x2="19" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </Link>
          ) : (
            <Link to="/register" className={styles.bannerBtn}>
              Tạo Tài Khoản Miễn Phí
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <line x1="5" y1="12" x2="19" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </Link>
          )}
        </div>

        {/* Contact Info Cards */}
        <div className={styles.contactGrid}>
          <a href="mailto:support@healthcare.io" className={styles.contactCard}>
            <div className={styles.contactIcon}>
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                <polyline points="22,6 12,13 2,6"/>
              </svg>
            </div>
            <div>
              <h4>Email</h4>
              <p>support@healthcare.io</p>
            </div>
          </a>

          <a href="tel:+84123456789" className={styles.contactCard}>
            <div className={styles.contactIcon}>
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
              </svg>
            </div>
            <div>
              <h4>Điện thoại</h4>
              <p>+84 (0) 123 456 789</p>
            </div>
          </a>

          <a href="https://zalo.me/healthcare" target="_blank" rel="noopener noreferrer" className={styles.contactCard}>
            <div className={styles.contactIcon}>
              {/* Zalo icon */}
              <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.04 2 11c0 2.76 1.36 5.22 3.48 6.87V22l3.6-2c.93.26 1.92.4 2.92.4 5.52 0 10-4.04 10-9S17.52 2 12 2zm1.13 12.22H8.58c-.31 0-.56-.25-.56-.56s.25-.56.56-.56h4.55c.31 0 .56.25.56.56s-.25.56-.56.56zm2.11-3H8.58c-.31 0-.56-.25-.56-.56s.25-.56.56-.56h6.66c.31 0 .56.25.56.56s-.25.56-.56.56zm0-3H8.58c-.31 0-.56-.25-.56-.56s.25-.56.56-.56h6.66c.31 0 .56.25.56.56s-.25.56-.56.56z"/>
              </svg>
            </div>
            <div>
              <h4>Zalo</h4>
              <p>HealthCare Support</p>
            </div>
          </a>
        </div>
      </div>
    </section>
  );
}
