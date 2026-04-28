import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchNotifications, markNotificationRead, markNotificationsRead } from '../services/notificationService.js';
import styles from './Phase3Pages.module.css';

function formatDateTime(value) {
  if (!value) return '-';
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

export default function NotificationPage() {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function loadNotifications() {
    setLoading(true);
    setError('');
    try {
      const response = await fetchNotifications({ size: 50 });
      setNotifications(response.data?.content ?? []);
      setUnreadCount(response.data?.unreadCount ?? 0);
    } catch (err) {
      setError(err.message ?? 'Unable to load notifications');
      setNotifications([]);
      setUnreadCount(0);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadNotifications();
  }, []);

  async function markOne(notificationId) {
    setSaving(true);
    setError('');
    try {
      await markNotificationRead(notificationId);
      await loadNotifications();
    } catch (err) {
      setError(err.message ?? 'Unable to update notification');
    } finally {
      setSaving(false);
    }
  }

  async function markAll() {
    const unreadIds = notifications.filter((item) => !item.read).map((item) => item.id);
    if (unreadIds.length === 0) return;
    setSaving(true);
    setError('');
    try {
      await markNotificationsRead(unreadIds);
      await loadNotifications();
    } catch (err) {
      setError(err.message ?? 'Unable to update notifications');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Notifications</p>
          <h1 className={styles.title}>Inbox</h1>
          <p className={styles.subtitle}>{unreadCount} unread appointment updates</p>
        </div>
        <div className={styles.actions}>
          <button className={styles.secondaryButton} type="button" onClick={loadNotifications} disabled={loading || saving}>
            Refresh
          </button>
          <button className={styles.primaryButton} type="button" onClick={markAll} disabled={loading || saving || unreadCount === 0}>
            Mark all read
          </button>
        </div>
      </div>

      {error && <div className={styles.alert}>{error}</div>}

      <section className={styles.panel}>
        <div className={styles.panelHeader}>
          <h2>Recent updates</h2>
          <span>{notifications.length} notifications</span>
        </div>

        {loading ? (
          <p className={styles.empty}>Loading notifications...</p>
        ) : notifications.length === 0 ? (
          <p className={styles.empty}>No notifications yet.</p>
        ) : (
          <div className={styles.list}>
            {notifications.map((notification) => (
              <article
                className={`${styles.item} ${notification.read ? '' : styles.unread}`}
                key={notification.id}
              >
                <div className={styles.itemTop}>
                  <div className={styles.itemTitle}>
                    <strong>{notification.title}</strong>
                    <span className={styles.meta}>{formatDateTime(notification.createdAt)}</span>
                  </div>
                  <span className={styles.badge}>{notification.read ? 'Read' : 'Unread'}</span>
                </div>
                <p>{notification.content}</p>
                <div className={styles.rowActions}>
                  {notification.appointmentId ? (
                    <Link className={styles.linkButton} to={`/appointments/${notification.appointmentId}`}>
                      Appointment
                    </Link>
                  ) : (
                    <span className={styles.meta}>{notification.type}</span>
                  )}
                  {!notification.read && (
                    <button className={styles.secondaryButton} type="button" onClick={() => markOne(notification.id)} disabled={saving}>
                      Mark read
                    </button>
                  )}
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  );
}
