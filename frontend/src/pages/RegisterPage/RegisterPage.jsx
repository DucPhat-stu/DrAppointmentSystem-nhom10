import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../../components/layout/AuthLayout.jsx';
import InputField from '../../components/forms/InputField.jsx';
import SelectField from '../../components/forms/SelectField.jsx';
import Button from '../../components/forms/Button.jsx';
import Divider from '../../components/forms/Divider.jsx';
import SocialButton from '../../components/forms/SocialButton.jsx';
import { useAuth } from '../../hooks/useAuth.js';
import { ApiError } from '../../services/httpClient.js';
import styles from './RegisterPage.module.css';

const COUNTRY_OPTIONS = [
  { value: 'VN', label: '🇻🇳 Vietnam' },
  { value: 'US', label: '🇺🇸 United States' },
  { value: 'JP', label: '🇯🇵 Japan' },
  { value: 'KR', label: '🇰🇷 South Korea' },
  { value: 'SG', label: '🇸🇬 Singapore' },
  { value: 'AU', label: '🇦🇺 Australia' },
  { value: 'UK', label: '🇬🇧 United Kingdom' },
  { value: 'DE', label: '🇩🇪 Germany' },
  { value: 'FR', label: '🇫🇷 France' },
  { value: 'CA', label: '🇨🇦 Canada' },
];

function validate(form) {
  const errors = {};

  if (!form.firstName.trim()) {
    errors.firstName = 'First name is required';
  }
  if (!form.lastName.trim()) {
    errors.lastName = 'Last name is required';
  }
  if (!form.username.trim()) {
    errors.username = 'Username is required';
  } else if (form.username.length < 3) {
    errors.username = 'Username must be at least 3 characters';
  }
  if (!form.email.trim()) {
    errors.email = 'Email is required';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = 'Please enter a valid email address';
  }
  if (!form.password) {
    errors.password = 'Password is required';
  } else if (form.password.length < 6) {
    errors.password = 'Password must be at least 6 characters';
  }
  if (!form.country) {
    errors.country = 'Please select your country';
  }

  return errors;
}

export default function RegisterPage() {
  const navigate = useNavigate();
  const { registerAction, isAuthenticated } = useAuth();

  // Redirect if already logged in
  if (isAuthenticated) {
    navigate('/doctors', { replace: true });
  }

  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    country: '',
  });
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
      await registerAction({
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password,
      });

      // Navigate to login after successful registration
      navigate('/login', { state: { registered: true } });
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.errorCode === 'CONFLICT' || err.status === 409) {
          setSubmitError('An account with this email already exists.');
        } else if (err.errorCode === 'VALIDATION_ERROR') {
          setSubmitError(err.message || 'Please check your input.');
        } else {
          setSubmitError(err.message || 'Registration failed. Please try again.');
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
      title="Create an account"
      subtitle="Join HealthCare today. Start managing your appointments with ease."
    >
      <form className={styles.form} onSubmit={handleSubmit} noValidate>
        {/* Name Row */}
        <div className={styles.row}>
          <InputField
            id="register-first-name"
            label="First Name"
            placeholder="John"
            value={form.firstName}
            onChange={handleChange('firstName')}
            error={errors.firstName}
            required
            icon={
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            }
          />
          <InputField
            id="register-last-name"
            label="Last Name"
            placeholder="Doe"
            value={form.lastName}
            onChange={handleChange('lastName')}
            error={errors.lastName}
            required
          />
        </div>

        {/* Username */}
        <InputField
          id="register-username"
          label="Username"
          placeholder="johndoe"
          value={form.username}
          onChange={handleChange('username')}
          error={errors.username}
          required
          autoComplete="username"
          icon={
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          }
        />

        {/* Email */}
        <InputField
          id="register-email"
          label="Email"
          type="email"
          placeholder="john@example.com"
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
          id="register-password"
          label="Password"
          type="password"
          placeholder="At least 6 characters"
          value={form.password}
          onChange={handleChange('password')}
          error={errors.password}
          required
          autoComplete="new-password"
          icon={
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
          }
        />

        {/* Country */}
        <SelectField
          id="register-country"
          label="Country"
          options={COUNTRY_OPTIONS}
          value={form.country}
          onChange={handleChange('country')}
          error={errors.country}
          placeholder="Select your country"
          required
        />

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
          id="register-submit"
          type="submit"
          variant="primary"
          size="lg"
          fullWidth
          loading={loading}
        >
          Create Account
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
          Already have an account?{' '}
          <Link to="/login" className={styles.link}>
            Sign in
          </Link>
        </p>
      </form>
    </AuthLayout>
  );
}
