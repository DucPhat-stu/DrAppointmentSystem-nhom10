import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import AuthLayout from '../components/layout/AuthLayout.jsx';
import InputField from '../components/forms/InputField.jsx';
import Button from '../components/forms/Button.jsx';
import { resetPassword } from '../services/authService.js';
import styles from './AuthUtilityPage.module.css';

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const [token, setToken] = useState(searchParams.get('token') || '');
  const [newPassword, setNewPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setMessage('');
    setError('');
    try {
      await resetPassword({ token, newPassword });
      setMessage('Password reset successfully. You can sign in with the new password.');
    } catch (err) {
      setError(err.message || 'Unable to reset password.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout title="Set new password" subtitle="Paste the mock token from auth-service logs.">
      <form className={styles.form} onSubmit={handleSubmit}>
        {message && <div className={styles.message}>{message}</div>}
        {error && <div className={styles.error}>{error}</div>}
        <InputField id="reset-token" label="Reset token" value={token} onChange={(event) => setToken(event.target.value)} required />
        <InputField id="reset-password" label="New password" type="password" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} required />
        <Button type="submit" size="lg" fullWidth loading={loading}>Update password</Button>
        <p className={styles.hint}>
          Return to <Link className={styles.link} to="/login">sign in</Link>
        </p>
      </form>
    </AuthLayout>
  );
}
