import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import AuthLayout from '../../components/layout/AuthLayout.jsx';
import InputField from '../../components/forms/InputField.jsx';
import Button from '../../components/forms/Button.jsx';
import Checkbox from '../../components/forms/Checkbox.jsx';
import Divider from '../../components/forms/Divider.jsx';
import SocialButton from '../../components/forms/SocialButton.jsx';
import { useAuth } from '../../hooks/useAuth.js';
import { ApiError } from '../../services/httpClient.js';
import * as authService from '../../services/authService.js';
import styles from './LoginPage.module.css';

function homePathForRole(role) {
  const normalizedRole = role?.replace(/^ROLE_/, '').toUpperCase();
  if (normalizedRole === 'DOCTOR') {
    return '/doctor/appointments';
  }
  if (normalizedRole === 'ADMIN') {
    return '/admin/prompts';
  }
  return '/doctors';
}

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
  const { loginAction, isAuthenticated, session, setSession } = useAuth();

  // Redirect if already logged in
  if (isAuthenticated) {
    navigate(homePathForRole(session?.role), { replace: true });
  }

  const justRegistered = location.state?.registered;

  const [form, setForm] = useState({
    email: '',
    password: '',
    phone: '',
    otp: '',
    doctorCode: 'DOCTOR-0001',
    twoFactorCode: '123456',
  });
  const [mode, setMode] = useState('password');
  const [otpRequested, setOtpRequested] = useState(false);
  const [infoMessage, setInfoMessage] = useState('');
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
    if (infoMessage) setInfoMessage('');
  };

  const storeSessionFromResponse = (response, fallback = {}) => {
    const data = response.data ?? {};
    const sessionData = {
      userId: data.userId ?? null,
      email: data.email ?? fallback.email ?? form.email,
      role: data.role ?? fallback.role ?? 'PATIENT',
      accessToken: data.accessToken ?? null,
      refreshToken: data.refreshToken ?? null,
      fullName: data.fullName ?? fallback.fullName ?? (fallback.email ?? form.email).split('@')[0],
    };
    setSession(sessionData);
    return sessionData;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationErrors = mode === 'password' || mode === 'twoFactor' ? validate(form) : {};
    if (mode === 'otp' && !form.phone.trim()) {
      validationErrors.phone = 'Phone is required';
    }
    if (mode === 'doctorCode' && !form.doctorCode.trim()) {
      validationErrors.doctorCode = 'Doctor code is required';
    }
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    setErrors({});
    setSubmitError('');

    try {
      const result = await loginAction({
        email: form.email,
        password: form.password,
      });

      navigate(homePathForRole(result.session?.role));
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.errorCode === 'UNAUTHORIZED' || err.status === 401) {
          setSubmitError('Invalid email or password. Please try again.');
        } else if (err.errorCode === 'VALIDATION_ERROR') {
          setSubmitError(err.message || 'Please check your input.');
        } else {
          setSubmitError(err.message || 'Login failed. Please try again.');
        }
      } else {
        setSubmitError('Unable to connect to server. Please try again later.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAlternateSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrors({});
    setSubmitError('');
    setInfoMessage('');

    try {
      if (mode === 'otp') {
        if (!otpRequested) {
          await authService.requestOtp(form.phone);
          setOtpRequested(true);
          setInfoMessage('Mock OTP sent. Use 123456 for demo.');
          return;
        }
        const response = await authService.verifyOtp({ phone: form.phone, otp: form.otp });
        const sessionData = storeSessionFromResponse(response, { email: form.phone });
        navigate(homePathForRole(sessionData.role));
        return;
      }

      if (mode === 'doctorCode') {
        const response = await authService.loginByDoctorCode(form.doctorCode);
        const sessionData = storeSessionFromResponse(response, { role: 'DOCTOR', email: 'doctor01@healthcare.local' });
        navigate(homePathForRole(sessionData.role));
        return;
      }

      if (mode === 'twoFactor') {
        const response = await authService.twoFactorLogin({
          email: form.email,
          password: form.password,
          code: form.twoFactorCode,
        });
        const sessionData = storeSessionFromResponse(response);
        navigate(homePathForRole(sessionData.role));
      }
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.errorCode === 'UNAUTHORIZED' || err.status === 401) {
          setSubmitError('Invalid email or password. Please try again.');
        } else if (err.errorCode === 'VALIDATION_ERROR') {
          setSubmitError(err.message || 'Please check your input.');
        } else {
          setSubmitError(err.message || 'Login failed. Please try again.');
        }
      } else {
        setSubmitError('Unable to connect to server. Please try again later.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Sign in to your account to continue."
    >
      <form className={styles.form} onSubmit={mode === 'password' ? handleSubmit : handleAlternateSubmit} noValidate>
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

        <div className={styles.tabs} role="tablist" aria-label="Login methods">
          {[
            ['password', 'Email'],
            ['otp', 'OTP'],
            ['doctorCode', 'Doctor'],
            ['twoFactor', '2FA'],
          ].map(([key, label]) => (
            <button
              key={key}
              type="button"
              className={`${styles.tabButton} ${mode === key ? styles.tabButtonActive : ''}`}
              onClick={() => {
                setMode(key);
                setSubmitError('');
                setInfoMessage('');
              }}
            >
              {label}
            </button>
          ))}
        </div>

        {/* Email */}
        {(mode === 'password' || mode === 'twoFactor') && <InputField
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
        />}

        {/* Password */}
        {(mode === 'password' || mode === 'twoFactor') && <InputField
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
        />}

        {mode === 'twoFactor' && (
          <InputField
            id="login-2fa"
            label="2FA code"
            value={form.twoFactorCode}
            onChange={handleChange('twoFactorCode')}
            error={errors.twoFactorCode}
            required
          />
        )}

        {mode === 'otp' && (
          <>
            <InputField
              id="login-phone"
              label="Phone"
              value={form.phone}
              onChange={handleChange('phone')}
              error={errors.phone}
              placeholder="0901000001"
              required
            />
            {otpRequested && (
              <InputField
                id="login-otp"
                label="OTP"
                value={form.otp}
                onChange={handleChange('otp')}
                placeholder="123456"
                required
              />
            )}
            <p className={styles.hint}>Demo OTP is always 123456.</p>
          </>
        )}

        {mode === 'doctorCode' && (
          <>
            <InputField
              id="login-doctor-code"
              label="Doctor code"
              value={form.doctorCode}
              onChange={handleChange('doctorCode')}
              error={errors.doctorCode}
              required
            />
            <p className={styles.hint}>Seed demo doctor code: DOCTOR-0001.</p>
          </>
        )}

        {/* Remember Me + Forgot Password */}
        {mode === 'password' && <div className={styles.options}>
          <Checkbox
            id="login-remember"
            label="Remember me"
            checked={rememberMe}
            onChange={(e) => setRememberMe(e.target.checked)}
          />
          <Link to="/forgot-password" className={styles.forgotLink}>
            Forgot password?
          </Link>
        </div>}

        {/* Submit Error */}
        {infoMessage && <div className={styles.successBanner}>{infoMessage}</div>}
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
          {mode === 'otp' && !otpRequested ? 'Send OTP' : 'Sign In'}
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
