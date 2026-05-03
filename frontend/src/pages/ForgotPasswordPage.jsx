import { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthLayout from '../components/layout/AuthLayout.jsx';
import InputField from '../components/forms/InputField.jsx';
import Button from '../components/forms/Button.jsx';
import { forgotPassword } from '../services/authService.js';
import styles from './AuthUtilityPage.module.css';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setMessage('');
    setError('');
    try {
      await forgotPassword(email);
      setMessage('If the email exists, a reset token was logged by auth-service for demo.');
    } catch (err) {
      setError(err.message || 'Unable to request a reset token.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout title="Reset password" subtitle="Use your account email to receive a mock reset token.">
      <form className={styles.form} onSubmit={handleSubmit}>
        {message && <div className={styles.message}>{message}</div>}
        {error && <div className={styles.error}>{error}</div>}
        <InputField
          id="forgot-email"
          label="Email"
          type="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="patient01@healthcare.local"
          required
        />
        <Button type="submit" size="lg" fullWidth loading={loading}>Send reset token</Button>
        <p className={styles.hint}>
          Already have a token? <Link className={styles.link} to="/reset-password">Set a new password</Link>
        </p>
      </form>
    </AuthLayout>
  );
}
