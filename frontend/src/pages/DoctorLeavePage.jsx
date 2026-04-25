import { useEffect, useMemo, useState } from 'react';
import { createDoctorLeave, fetchDoctorLeaves } from '../services/doctorService.js';
import styles from './DoctorLeavePage.module.css';

function today() {
  return new Date().toISOString().slice(0, 10);
}

function tomorrow() {
  const date = new Date();
  date.setDate(date.getDate() + 1);
  return date.toISOString().slice(0, 10);
}

function formatDate(value) {
  if (!value) return '-';
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(`${value}T00:00:00`));
}

export default function DoctorLeavePage() {
  const [form, setForm] = useState({ startDate: today(), endDate: tomorrow() });
  const [leaves, setLeaves] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const pendingCount = useMemo(
    () => leaves.filter((leave) => leave.status === 'PENDING').length,
    [leaves],
  );

  async function loadLeaves() {
    setLoading(true);
    setError('');
    try {
      const response = await fetchDoctorLeaves();
      setLeaves(response.data ?? []);
    } catch (err) {
      setError(err.message ?? 'Unable to load leave requests');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadLeaves();
  }, []);

  async function submitLeave(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    setMessage('');

    try {
      await createDoctorLeave(form);
      setMessage('Leave request submitted.');
      setForm({ startDate: today(), endDate: tomorrow() });
      await loadLeaves();
    } catch (err) {
      setError(err.message ?? 'Unable to submit leave request');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Doctor Leave</p>
          <h1 className={styles.title}>Leave requests</h1>
        </div>
        <button className={styles.secondaryButton} type="button" onClick={loadLeaves} disabled={loading || saving}>
          Refresh
        </button>
      </div>

      {error && <div className={styles.alert}>{error}</div>}
      {message && <div className={styles.success}>{message}</div>}

      <div className={styles.layout}>
        <form className={styles.panel} onSubmit={submitLeave}>
          <div className={styles.panelHeader}>
            <h2>New request</h2>
            <span>{pendingCount} pending</span>
          </div>
          <label>
            <span>Start date</span>
            <input
              type="date"
              value={form.startDate}
              onChange={(event) => setForm((current) => ({ ...current, startDate: event.target.value }))}
              disabled={saving}
              required
            />
          </label>
          <label>
            <span>End date</span>
            <input
              type="date"
              value={form.endDate}
              onChange={(event) => setForm((current) => ({ ...current, endDate: event.target.value }))}
              disabled={saving}
              required
            />
          </label>
          <button className={styles.primaryButton} type="submit" disabled={saving}>
            Submit request
          </button>
        </form>

        <section className={styles.panel}>
          <div className={styles.panelHeader}>
            <h2>History</h2>
            <span>{leaves.length} requests</span>
          </div>

          {loading ? (
            <p className={styles.empty}>Loading leave requests...</p>
          ) : leaves.length === 0 ? (
            <p className={styles.empty}>No leave requests yet.</p>
          ) : (
            <div className={styles.list}>
              {leaves.map((leave) => (
                <article className={styles.row} key={leave.id}>
                  <div>
                    <strong>{formatDate(leave.startDate)} - {formatDate(leave.endDate)}</strong>
                    <span>{leave.rejectionReason || 'No decision note'}</span>
                  </div>
                  <em className={`${styles.status} ${styles[leave.status?.toLowerCase()] ?? ''}`}>{leave.status}</em>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </section>
  );
}
