import { useEffect, useRef, useState } from 'react';
import { useScrollReveal } from '../../hooks/useScrollReveal.js';
import styles from './AboutSection.module.css';

/**
 * Animated counter that counts up from 0 to `end` when visible.
 */
function AnimatedCounter({ end, suffix = '', duration = 2000, isVisible }) {
  const [count, setCount] = useState(0);
  const frameRef = useRef(null);

  useEffect(() => {
    if (!isVisible) {
      setCount(0);
      return;
    }

    const startTime = performance.now();
    const numericEnd = parseFloat(end);

    function animate(currentTime) {
      const elapsed = currentTime - startTime;
      const progress = Math.min(elapsed / duration, 1);
      // Ease-out cubic
      const eased = 1 - Math.pow(1 - progress, 3);
      const currentVal = eased * numericEnd;

      // Handle decimals (e.g. 99.9)
      if (String(end).includes('.')) {
        setCount(currentVal.toFixed(1));
      } else {
        setCount(Math.floor(currentVal));
      }

      if (progress < 1) {
        frameRef.current = requestAnimationFrame(animate);
      }
    }

    frameRef.current = requestAnimationFrame(animate);

    return () => {
      if (frameRef.current) cancelAnimationFrame(frameRef.current);
    };
  }, [end, duration, isVisible]);

  return (
    <span>
      {count}
      {suffix}
    </span>
  );
}

const STATS = [
  { value: 12, suffix: '', label: 'Microservices' },
  { value: 99.9, suffix: '%', label: 'Uptime' },
  { value: 256, suffix: '-bit', label: 'Encryption' },
];

const FEATURES = [
  {
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
      </svg>
    ),
    title: 'Kiến trúc Microservices',
    desc: 'Hệ thống phân tán đảm bảo hiệu suất và khả năng mở rộng.',
  },
  {
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
      </svg>
    ),
    title: 'Tuân thủ HIPAA',
    desc: 'Thiết kế theo tiêu chuẩn bảo mật ngành y tế quốc tế.',
  },
  {
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
        <line x1="8" y1="21" x2="16" y2="21"/>
        <line x1="12" y1="17" x2="12" y2="21"/>
      </svg>
    ),
    title: 'Đa nền tảng',
    desc: 'Truy cập dễ dàng từ máy tính, tablet và điện thoại di động.',
  },
];

export default function AboutSection() {
  const { ref, isVisible } = useScrollReveal({ threshold: 0.1 });

  return (
    <section
      ref={ref}
      className={`${styles.section} ${isVisible ? styles.visible : ''}`}
      id="about"
    >
      <div className={styles.container}>
        <div className={styles.grid}>
          {/* Left – Content */}
          <div className={styles.content}>
            <span className={styles.badge}>Về Chúng Tôi</span>
            <h2 className={styles.title}>
              Đổi Mới Y Tế Cho{' '}
              <span className={styles.highlight}>Thời Đại Số</span>
            </h2>
            <p className={styles.text}>
              HealthCare Platform ra đời với sứ mệnh: mang dịch vụ y tế chất lượng
              đến gần hơn với mọi người. Nền tảng của chúng tôi kết nối bệnh nhân và
              bác sĩ thông qua công nghệ hiện đại, cung cấp quản lý lịch hẹn liền mạch,
              hồ sơ sức khỏe bảo mật và thông báo theo thời gian thực.
            </p>
            <p className={styles.text}>
              Chúng tôi sử dụng kiến trúc microservices để đảm bảo độ tin cậy, khả năng
              mở rộng và bảo mật dữ liệu ở mức cao nhất. Mọi tính năng đều được thiết kế
              với trải nghiệm bệnh nhân là trung tâm.
            </p>

            {/* Counter Stats */}
            <div className={styles.stats}>
              {STATS.map((stat, i) => (
                <div key={i} className={styles.stat}>
                  <span className={styles.statValue}>
                    <AnimatedCounter
                      end={stat.value}
                      suffix={stat.suffix}
                      isVisible={isVisible}
                    />
                  </span>
                  <span className={styles.statLabel}>{stat.label}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Right – Feature Cards */}
          <div className={styles.visual}>
            {FEATURES.map((feature, i) => (
              <div
                key={i}
                className={styles.featureCard}
                style={{ animationDelay: isVisible ? `${300 + i * 150}ms` : '0ms' }}
              >
                <div className={styles.featureIcon}>{feature.icon}</div>
                <div className={styles.featureText}>
                  <h4>{feature.title}</h4>
                  <p>{feature.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
