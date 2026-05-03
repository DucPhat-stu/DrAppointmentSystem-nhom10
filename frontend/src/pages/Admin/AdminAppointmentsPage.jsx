import { useEffect, useMemo, useState } from 'react';
import { adminCancelAppointment, fetchAdminAppointments } from '../../services/adminService.js';
import styles from './AdminPage.module.css';

const STATUSES = ['', 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'REJECTED'];

const STATUS_MAP = {
  PENDING: styles.badgeYellow,
  CONFIRMED: styles.badgeBlue,
  COMPLETED: styles.badgeGreen,
  CANCELLED: styles.badgeRed,
  REJECTED: styles.badgeOrange,
};

function fmt(value) {
  if (!value) return '-';
  return new Intl.DateTimeFormat('en-US', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
}

export default function AdminAppointmentsPage() {
  const [filters, setFilters] = useState({ status: '', page: 0, size: 20 });
  const [pageData, setPageData] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [cancelTarget, setCancelTarget] = useState(null);
  const [cancelReason, setCancelReason] = useState('');
  const [savingId, setSavingId] = useState(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const canPrev = filters.page > 0;
  const canNext = useMemo(() => pageData.totalPages > 0 && filters.page < pageData.totalPages - 1, [filters.page, pageData.totalPages]);

  async function load(nextFilters = filters) {
    setLoading(true);
    setError('');
    setMessage('');
    try {
      const response = await fetchAdminAppointments(nextFilters);
      setPageData(response.data ?? { content: [], totalElements: 0, totalPages: 0 });
    } catch (err) {
      setError(err.message ?? 'Cannot load appointments');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function apply(event) {
    event.preventDefault();
    const next = { ...filters, page: 0 };
    setFilters(next);
    await load(next);
  }

  async function movePage(delta) {
    const next = { ...filters, page: filters.page + delta };
    setFilters(next);
    await load(next);
  }

  async function confirmCancel(event) {
    event.preventDefault();
    if (!cancelTarget) return;
    setSavingId(cancelTarget.id);
    setError('');
    try {
      await adminCancelAppointment(cancelTarget.id, cancelReason);
      setMessage(`Appointment ${cancelTarget.id.substring(0, 8)} was cancelled`);
      setCancelTarget(null);
      setCancelReason('');
      await load();
    } catch (err) {
      setError(err.message ?? 'Cancel failed');
    } finally {
      setSavingId(null);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>Appointment Management</h1>
          <p className={styles.pageSub}>{pageData.totalElements} total appointments</p>
        </div>
      </div>

      {message && <div className={styles.alert + ' ' + styles.alertSuccess}>{message}</div>}
      {error && <div className={styles.alert + ' ' + styles.alertError}>{error}</div>}

      <form className={styles.filterBar} onSubmit={apply}>
        <label className={styles.filterItem}>
          <span>Status</span>
          <select value={filters.status} onChange={(event) => setFilters((current) => ({ ...current, status: event.target.value }))}>
            {STATUSES.map((status) => <option key={status || 'ALL'} value={status}>{status || 'All'}</option>)}
          </select>
        </label>
        <button className={styles.btnPrimary} type="submit" disabled={loading}>Filter</button>
        <button className={styles.btnSecondary} type="button" onClick={() => load()} disabled={loading}>Refresh</button>
      </form>

      <div className={styles.tableCard}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Patient</th>
              <th>Doctor</th>
              <th>Start</th>
              <th>End</th>
              <th>Status</th>
              <th>Cancel</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={7} className={styles.emptyCell}>Loading...</td></tr>
            ) : pageData.content.length === 0 ? (
              <tr><td colSpan={7} className={styles.emptyCell}>No appointments found.</td></tr>
            ) : pageData.content.map((appointment) => (
              <tr key={appointment.id}>
                <td className={styles.idCell} title={appointment.id}>{appointment.id?.substring(0, 8)}...</td>
                <td className={styles.idCell} title={appointment.patientId}>{appointment.patientId?.substring(0, 8)}...</td>
                <td className={styles.idCell} title={appointment.doctorId}>{appointment.doctorId?.substring(0, 8)}...</td>
                <td className={styles.dateCell}>{fmt(appointment.scheduledStart)}</td>
                <td className={styles.dateCell}>{fmt(appointment.scheduledEnd)}</td>
                <td><span className={`${styles.badge} ${STATUS_MAP[appointment.status] ?? ''}`}>{appointment.status}</span></td>
                <td>
                  {(appointment.status === 'PENDING' || appointment.status === 'CONFIRMED') && (
                    <button
                      className={styles.btnDanger}
                      type="button"
                      disabled={savingId === appointment.id}
                      onClick={() => {
                        setCancelTarget(appointment);
                        setCancelReason('');
                      }}
                    >
                      Cancel
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className={styles.pager}>
          <button className={styles.btnSecondary} type="button" disabled={!canPrev || loading} onClick={() => movePage(-1)}>Previous</button>
          <span className={styles.pageInfo}>Page {pageData.totalPages === 0 ? 0 : filters.page + 1} / {pageData.totalPages}</span>
          <button className={styles.btnSecondary} type="button" disabled={!canNext || loading} onClick={() => movePage(1)}>Next</button>
        </div>
      </div>

      {cancelTarget && (
        <div className={styles.modalBackdrop}>
          <form className={styles.modal} onSubmit={confirmCancel}>
            <h2 className={styles.modalTitle}>Cancel appointment</h2>
            <p className={styles.modalSub}>ID: <code>{cancelTarget.id}</code></p>
            <p className={styles.modalSub}>Start: {fmt(cancelTarget.scheduledStart)}</p>
            <label className={styles.modalLabel}>
              <span>Cancellation reason</span>
              <textarea
                value={cancelReason}
                onChange={(event) => setCancelReason(event.target.value)}
                rows={3}
                placeholder="Enter a reason..."
                maxLength={500}
              />
            </label>
            <div className={styles.modalActions}>
              <button className={styles.btnSecondary} type="button" onClick={() => setCancelTarget(null)}>Close</button>
              <button className={styles.btnDanger} type="submit" disabled={!!savingId}>Confirm cancellation</button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
