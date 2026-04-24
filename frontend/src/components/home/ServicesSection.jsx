import { useScrollReveal } from '../../hooks/useScrollReveal.js';
import styles from './ServicesSection.module.css';

const SERVICES = [
  {
    icon: (
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
        <circle cx="9" cy="7" r="4"/>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
        <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
      </svg>
    ),
    title: 'Tư vấn bác sĩ',
    desc: 'Kết nối trực tiếp với hơn 500 bác sĩ chuyên khoa uy tín, trải rộng 20+ chuyên ngành y tế.',
  },
  {
    icon: (
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
        <line x1="16" y1="2" x2="16" y2="6"/>
        <line x1="8" y1="2" x2="8" y2="6"/>
        <line x1="3" y1="10" x2="21" y2="10"/>
        <path d="M8 14h.01"/>
        <path d="M12 14h.01"/>
        <path d="M16 14h.01"/>
        <path d="M8 18h.01"/>
        <path d="M12 18h.01"/>
      </svg>
    ),
    title: 'Đặt lịch khám',
    desc: 'Đặt lịch hẹn khám bệnh nhanh chóng với hệ thống hiển thị lịch trống theo thời gian thực.',
  },
  {
    icon: (
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
        <polyline points="14 2 14 8 20 8"/>
        <line x1="16" y1="13" x2="8" y2="13"/>
        <line x1="16" y1="17" x2="8" y2="17"/>
        <polyline points="10 9 9 9 8 9"/>
      </svg>
    ),
    title: 'Hồ sơ sức khỏe',
    desc: 'Quản lý hồ sơ bệnh án điện tử an toàn, truy cập mọi lúc mọi nơi qua nền tảng bảo mật.',
  },
  {
    icon: (
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      </svg>
    ),
    title: 'Nhắc lịch thông minh',
    desc: 'Hệ thống nhắc lịch tự động giúp bạn không bao giờ bỏ lỡ cuộc hẹn khám bệnh quan trọng.',
  },
  {
    icon: (
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
      </svg>
    ),
    title: 'Bảo mật dữ liệu',
    desc: 'Mã hoá đầu cuối và tuân thủ tiêu chuẩn bảo mật y tế HIPAA, bảo vệ thông tin cá nhân tuyệt đối.',
  },
  {
    icon: (
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="10"/>
        <polyline points="12 6 12 12 16 14"/>
      </svg>
    ),
    title: 'Hoạt động 24/7',
    desc: 'Truy cập nền tảng bất kỳ lúc nào — đặt lịch, kiểm tra và quản lý sức khỏe mọi nơi.',
  },
];

export default function ServicesSection() {
  const { ref, isVisible } = useScrollReveal({ threshold: 0.1 });

  return (
    <section
      ref={ref}
      className={`${styles.section} ${isVisible ? styles.visible : ''}`}
      id="services"
    >
      <div className={styles.container}>
        {/* Header */}
        <div className={styles.header}>
          <span className={styles.badge}>Dịch Vụ Của Chúng Tôi</span>
          <h2 className={styles.title}>
            Tất Cả Những Gì Bạn Cần Cho{' '}
            <span className={styles.highlight}>Sức Khỏe Tốt Hơn</span>
          </h2>
          <p className={styles.subtitle}>
            Dịch vụ y tế toàn diện được thiết kế xoay quanh nhu cầu chăm sóc sức khỏe của bạn
          </p>
        </div>

        {/* Grid */}
        <div className={styles.grid}>
          {SERVICES.map((service, i) => (
            <div
              key={i}
              className={styles.card}
              style={{ animationDelay: isVisible ? `${i * 100}ms` : '0ms' }}
            >
              <div className={styles.cardIcon}>{service.icon}</div>
              <h3 className={styles.cardTitle}>{service.title}</h3>
              <p className={styles.cardDesc}>{service.desc}</p>
              <div className={styles.cardArrow}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="5" y1="12" x2="19" y2="12"/>
                  <polyline points="12 5 19 12 12 19"/>
                </svg>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
