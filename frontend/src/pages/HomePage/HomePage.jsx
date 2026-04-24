import Navbar from '../../components/layout/Navbar.jsx';
import HeroSection from '../../components/home/HeroSection.jsx';
import Footer from '../../components/layout/Footer.jsx';
import styles from './HomePage.module.css';

export default function HomePage() {
  return (
    <div className={styles.page}>
      <Navbar />

      <HeroSection />

      {/* Services Section */}
      <section className={styles.section} id="services">
        <div className={styles.sectionContainer}>
          <div className={styles.sectionHeader}>
            <span className={styles.sectionBadge}>Our Services</span>
            <h2 className={styles.sectionTitle}>
              Everything You Need for{' '}
              <span className={styles.highlight}>Better Health</span>
            </h2>
            <p className={styles.sectionSubtitle}>
              Comprehensive healthcare services designed around your needs
            </p>
          </div>

          <div className={styles.servicesGrid}>
            {[
              {
                icon: (
                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                  </svg>
                ),
                title: 'Doctor Consultations',
                desc: 'Connect with certified specialists across 20+ medical departments.',
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
                title: 'Appointment Booking',
                desc: 'Schedule and manage appointments with real-time slot availability.',
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
                title: 'Health Records',
                desc: 'Secure digital health records accessible anywhere, anytime.',
              },
              {
                icon: (
                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                    <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                  </svg>
                ),
                title: 'Smart Notifications',
                desc: 'Automated reminders so you never miss an important appointment.',
              },
              {
                icon: (
                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                  </svg>
                ),
                title: 'Data Security',
                desc: 'End-to-end encryption and HIPAA-compliant data protection.',
              },
              {
                icon: (
                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <polyline points="12 6 12 12 16 14"/>
                  </svg>
                ),
                title: '24/7 Availability',
                desc: 'Access our platform anytime — book, check, and manage on the go.',
              },
            ].map((service, i) => (
              <div key={i} className={styles.serviceCard}>
                <div className={styles.serviceIcon}>{service.icon}</div>
                <h3 className={styles.serviceTitle}>{service.title}</h3>
                <p className={styles.serviceDesc}>{service.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* About Section */}
      <section className={styles.section} id="about">
        <div className={styles.sectionContainer}>
          <div className={styles.aboutGrid}>
            <div className={styles.aboutContent}>
              <span className={styles.sectionBadge}>About Us</span>
              <h2 className={styles.sectionTitle}>
                Reimagining Healthcare for the{' '}
                <span className={styles.highlight}>Digital Age</span>
              </h2>
              <p className={styles.aboutText}>
                HealthCare Platform was built with one mission: to make quality healthcare
                accessible to everyone. Our platform bridges the gap between patients and
                doctors through modern technology, providing seamless appointment management,
                secure health records, and real-time communication.
              </p>
              <p className={styles.aboutText}>
                We leverage microservices architecture to ensure reliability, scalability,
                and the highest level of data security. Every feature is designed with
                patient experience at the center.
              </p>
              <div className={styles.aboutStats}>
                <div className={styles.aboutStat}>
                  <span className={styles.aboutStatValue}>12</span>
                  <span className={styles.aboutStatLabel}>Microservices</span>
                </div>
                <div className={styles.aboutStat}>
                  <span className={styles.aboutStatValue}>99.9%</span>
                  <span className={styles.aboutStatLabel}>Uptime</span>
                </div>
                <div className={styles.aboutStat}>
                  <span className={styles.aboutStatValue}>256-bit</span>
                  <span className={styles.aboutStatLabel}>Encryption</span>
                </div>
              </div>
            </div>
            <div className={styles.aboutVisual}>
              <div className={styles.aboutCard}>
                <div className={styles.aboutCardIcon}>
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
                  </svg>
                </div>
                <h3>Built for Healthcare</h3>
                <p>Designed following medical industry standards and best practices with HIPAA compliance in mind.</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Contact Section */}
      <section className={styles.section} id="contact">
        <div className={styles.sectionContainer}>
          <div className={styles.sectionHeader}>
            <span className={styles.sectionBadge}>Get in Touch</span>
            <h2 className={styles.sectionTitle}>
              Ready to <span className={styles.highlight}>Get Started</span>?
            </h2>
            <p className={styles.sectionSubtitle}>
              Join thousands of patients who trust HealthCare Platform
            </p>
          </div>

          <div className={styles.ctaBanner}>
            <div className={styles.ctaBannerContent}>
              <h3>Start your health journey today</h3>
              <p>Create a free account and book your first appointment in minutes.</p>
            </div>
            <a href="/register" className={styles.ctaBannerBtn}>
              Create Free Account
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <line x1="5" y1="12" x2="19" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </a>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
}
