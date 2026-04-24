import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import AuthLayout from '../../components/layout/AuthLayout.jsx';
import InputField from '../../components/forms/InputField.jsx';
import Button from '../../components/forms/Button.jsx';
import Checkbox from '../../components/forms/Checkbox.jsx';
import Divider from '../../components/forms/Divider.jsx';
import SocialButton from '../../components/forms/SocialButton.jsx';
import { useAuth } from '../../hooks/useAuth.js';
import styles from './LoginPage.module.css';

function validate(form) {
  const errors = {};

  if (!form.email.trim()) {
    errors.email = 'Email is required';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = 'Please enter a valid email address';
  }
  if (!form.password) {
    errors.password = 'Password is required';
  }

  return errors;
}

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { setSession } = useAuth();

  const justRegistered = location.state?.registered;

  const [form, setForm] = useState({
    email: '',
    password: '',
  });
  const [rememberMe, setRememberMe] = useState(false);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [submitError, setSubmitError] = useState('');

  const handleChange = (field) => (e) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
    if (errors[field]) {
      setErrors((prev) => {
        const next = { ...prev };
        delete next[field];
        return next;
      });
    }
    if (submitError) setSubmitError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationErrors = validate(form);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    setErrors({});
    setSubmitError('');

    try {
      // TODO: Replace with real API call
      // const response = await authService.login({
      //   email: form.email,
      //   password: form.password,
      //   actor: 'PATIENT',
      // });
      await new Promise((resolve) => setTimeout(resolve, 1200));

      // Mock session for MVP – will connect to real auth-service API
      setSession({
        email: form.email,
        role: 'PATIENT',
        accessToken: 'mock-jwt-token',
      });

      navigate('/doctors');
    } catch (err) {
      setSubmitError(err.message || 'Invalid email or password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Sign in to your account to continue."
    >
      <form className={styles.form} onSubmit={handleSubmit} noValidate>
        {/* Success banner after registration */}
        {justRegistered && (
          <div className={styles.successBanner}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
            Account created successfully! Please sign in.
          </div>
        )}

        {/* Email */}
        <InputField
          id="login-email"
          label="Email"
          type="email"
          placeholder="Enter your email"
          value={form.email}
          onChange={handleChange('email')}
          error={errors.email}
          required
          autoComplete="email"
          icon={
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </svg>
          }
        />

        {/* Password */}
        <InputField
          id="login-password"
          label="Password"
          type="password"
          placeholder="Enter your password"
          value={form.password}
          onChange={handleChange('password')}
          error={errors.password}
          required
          autoComplete="current-password"
          icon={
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
          }
        />

        {/* Remember Me + Forgot Password */}
        <div className={styles.options}>
          <Checkbox
            id="login-remember"
            label="Remember me"
            checked={rememberMe}
            onChange={(e) => setRememberMe(e.target.checked)}
          />
          <a href="#" className={styles.forgotLink} onClick={(e) => e.preventDefault()}>
            Forgot password?
          </a>
        </div>

        {/* Submit Error */}
        {submitError && (
          <div className={styles.submitError}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
            {submitError}
          </div>
        )}

        {/* Submit Button */}
        <Button
          id="login-submit"
          type="submit"
          variant="primary"
          size="lg"
          fullWidth
          loading={loading}
        >
          Sign In
        </Button>

        {/* Divider */}
        <Divider text="or continue with" />

        {/* Social Buttons */}
        <div className={styles.socialRow}>
          <SocialButton provider="google" onClick={() => {}} />
          <SocialButton provider="apple" onClick={() => {}} />
        </div>

        {/* Footer Link */}
        <p className={styles.footer}>
          Don&apos;t have an account?{' '}
          <Link to="/register" className={styles.link}>
            Create one
          </Link>
        </p>
      </form>
    </AuthLayout>
  );
}
