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
      setMessage(`Đã gửi ${response.data?.created ?? recipientIds.length} thông báo.`);
      setForm({ recipientIds: '', title: '', content: '' });
    } catch (err) {
      setError(err.message || 'Không thể gửi thông báo hàng loạt.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>Thông báo hàng loạt</h1>
          <p className={styles.pageSub}>Nhập danh sách user ID, phân tách bằng dấu phẩy.</p>
        </div>
      </div>
      {message && <div className={styles.alert + ' ' + styles.alertSuccess}>{message}</div>}
      {error && <div className={styles.alert + ' ' + styles.alertError}>{error}</div>}
      <form className={styles.filterBar} onSubmit={submit}>
        <label className={styles.filterItem}>
          <span>Recipient IDs</span>
          <input value={form.recipientIds} onChange={(e) => setForm((f) => ({ ...f, recipientIds: e.target.value }))} required />
        </label>
        <label className={styles.filterItem}>
          <span>Tiêu đề</span>
          <input value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} required />
        </label>
        <label className={styles.filterItem}>
          <span>Nội dung</span>
          <input value={form.content} onChange={(e) => setForm((f) => ({ ...f, content: e.target.value }))} required />
        </label>
        <button className={styles.btnPrimary} type="submit" disabled={saving}>Gửi broadcast</button>
      </form>
    </div>
  );
}
