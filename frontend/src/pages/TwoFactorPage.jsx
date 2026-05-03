import { useState } from 'react';
import { setupTwoFactor, verifyTwoFactor } from '../services/authService.js';
import InputField from '../components/forms/InputField.jsx';
import Button from '../components/forms/Button.jsx';
import styles from './AuthUtilityPage.module.css';

export default function TwoFactorPage() {
  const [setup, setSetup] = useState(null);
  const [code, setCode] = useState('123456');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function handleSetup() {
    setLoading(true);
    setError('');
    setMessage('');
    try {
      const response = await setupTwoFactor();
      setSetup(response.data);
      setMessage('Mock 2FA secret created. Use code 123456 to enable it.');
    } catch (err) {
      setError(err.message || 'Unable to set up 2FA.');
    } finally {
      setLoading(false);
    }
  }

  async function handleVerify(event) {
    event.preventDefault();
    setLoading(true);
    setError('');
    setMessage('');
    try {
      await verifyTwoFactor(code);
      setMessage('2FA enabled for this account.');
    } catch (err) {
      setError(err.message || 'Unable to verify 2FA.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section>
      <h1>Two-factor authentication</h1>
      <p className={styles.hint}>Demo mode uses TOTP code 123456.</p>
      {message && <div className={styles.message}>{message}</div>}
      {error && <div className={styles.error}>{error}</div>}
      <form className={styles.form} onSubmit={handleVerify}>
        <Button type="button" onClick={handleSetup} loading={loading}>Create mock setup</Button>
        {setup?.secret && <p className={styles.hint}>Secret: {setup.secret}</p>}
        <InputField id="twofactor-code" label="Verification code" value={code} onChange={(event) => setCode(event.target.value)} />
        <Button type="submit" loading={loading}>Enable 2FA</Button>
      </form>
    </section>
  );
}
