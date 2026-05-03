import { useState } from 'react';
import { broadcastNotification } from '../../services/notificationService.js';
import styles from './AdminPage.module.css';

export default function AdminNotificationsPage() {
  const [form, setForm] = useState({ recipientIds: '', title: '', content: '' });
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setMessage('');
    setError('');
    try {
      const recipientIds = form.recipientIds.split(',').map((value) => value.trim()).filter(Boolean);
      const response = await broadcastNotification({ recipientIds, title: form.title, content: form.content });
      setMessage(`Sent ${response.data?.created ?? recipientIds.length} notifications.`);
      setForm({ recipientIds: '', title: '', content: '' });
    } catch (err) {
      setError(err.message || 'Unable to send broadcast notifications.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>Broadcast Notifications</h1>
          <p className={styles.pageSub}>Enter user IDs separated by commas.</p>
        </div>
      </div>
      {message && <div className={styles.alert + ' ' + styles.alertSuccess}>{message}</div>}
      {error && <div className={styles.alert + ' ' + styles.alertError}>{error}</div>}
      <form className={styles.filterBar} onSubmit={submit}>
        <label className={styles.filterItem}>
          <span>Recipient IDs</span>
          <input value={form.recipientIds} onChange={(event) => setForm((current) => ({ ...current, recipientIds: event.target.value }))} required />
        </label>
        <label className={styles.filterItem}>
          <span>Title</span>
          <input value={form.title} onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} required />
        </label>
        <label className={styles.filterItem}>
          <span>Content</span>
          <input value={form.content} onChange={(event) => setForm((current) => ({ ...current, content: event.target.value }))} required />
        </label>
        <button className={styles.btnPrimary} type="submit" disabled={saving}>Send broadcast</button>
      </form>
    </div>
  );
}
