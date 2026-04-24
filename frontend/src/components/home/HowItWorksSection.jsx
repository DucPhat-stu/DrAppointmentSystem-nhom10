import { useScrollReveal } from '../../hooks/useScrollReveal.js';
import styles from './HowItWorksSection.module.css';

const STEPS = [
  {
    number: '01',
    icon: (
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
        <circle cx="8.5" cy="7" r="4"/>
        <line x1="20" y1="8" x2="20" y2="14"/>
        <line x1="23" y1="11" x2="17" y2="11"/>
      </svg>
    ),
    title: 'Tạo tài khoản',
    desc: 'Đăng ký tài khoản miễn phí chỉ trong vài giây với email và mật khẩu.',
  },
  {
    number: '02',
    icon: (
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="11" cy="11" r="8"/>
        <line x1="21" y1="21" x2="16.65" y2="16.65"/>
        <line x1="11" y1="8" x2="11" y2="14"/>
        <line x1="8" y1="11" x2="14" y2="11"/>
      </svg>
    ),
    title: 'Tìm bác sĩ',
    desc: 'Tìm kiếm bác sĩ chuyên khoa phù hợp theo chuyên ngành và khu vực.',
  },
  {
    number: '03',
    icon: (
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
        <line x1="16" y1="2" x2="16" y2="6"/>
        <line x1="8" y1="2" x2="8" y2="6"/>
        <line x1="3" y1="10" x2="21" y2="10"/>
        <path d="M9 16l2 2 4-4"/>
      </svg>
    ),
    title: 'Đặt lịch hẹn',
    desc: 'Chọn ngày giờ phù hợp và xác nhận lịch hẹn khám bệnh chỉ với vài click.',
  },
  {
    number: '04',
    icon: (
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
        <polyline points="22 4 12 14.01 9 11.01"/>
      </svg>
    ),
    title: 'Khám bệnh',
    desc: 'Đến gặp bác sĩ đúng giờ, nhận kết quả và theo dõi sức khỏe trên nền tảng.',
  },
];

export default function HowItWorksSection() {
  const { ref, isVisible } = useScrollReveal({ threshold: 0.1 });

  return (
    <section
      ref={ref}
      className={`${styles.section} ${isVisible ? styles.visible : ''}`}
      id="how-it-works"
    >
      <div className={styles.container}>
        {/* Header */}
        <div className={styles.header}>
          <span className={styles.badge}>Quy Trình</span>
          <h2 className={styles.title}>
            Đặt Lịch Khám Bệnh{' '}
            <span className={styles.highlight}>Chỉ 4 Bước</span>
          </h2>
          <p className={styles.subtitle}>
            Quy trình đơn giản, nhanh chóng giúp bạn tiết kiệm thời gian
          </p>
        </div>

        {/* Steps */}
        <div className={styles.steps}>
          {STEPS.map((step, i) => (
            <div
              key={i}
              className={styles.step}
              style={{ animationDelay: isVisible ? `${i * 150}ms` : '0ms' }}
            >
              <div className={styles.stepNumber}>{step.number}</div>
              <div className={styles.stepIcon}>{step.icon}</div>
              <h3 className={styles.stepTitle}>{step.title}</h3>
              <p className={styles.stepDesc}>{step.desc}</p>

              {/* Connector line */}
              {i < STEPS.length - 1 && (
                <div className={styles.connector} aria-hidden="true" />
              )}
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
