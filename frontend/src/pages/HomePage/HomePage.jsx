import Navbar from '../../components/layout/Navbar.jsx';
import HeroSection from '../../components/home/HeroSection.jsx';
import ServicesSection from '../../components/home/ServicesSection.jsx';
import HowItWorksSection from '../../components/home/HowItWorksSection.jsx';
import AboutSection from '../../components/home/AboutSection.jsx';
import ContactSection from '../../components/home/ContactSection.jsx';
import Footer from '../../components/layout/Footer.jsx';
import styles from './HomePage.module.css';

export default function HomePage() {
  return (
    <div className={styles.page}>
      <Navbar />
      <HeroSection />
      <ServicesSection />
      <HowItWorksSection />
      <AboutSection />
      <ContactSection />
      <Footer />
    </div>
  );
}
