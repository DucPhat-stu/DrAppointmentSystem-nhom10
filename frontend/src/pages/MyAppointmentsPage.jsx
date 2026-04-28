import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchAppointments } from '../services/appointmentService.js';
import styles from './Phase3Pages.module.css';

const STATUSES = ['', 'PENDING', 'CONFIRMED', 'CANCELLED', 'REJECTED', 'COMPLETED'];

function formatDateTime(value) {
  if (!value) return '-';
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

function statusClass(status) {
  return styles[(status ?? '').toLowerCase()] ?? '';
}

export default function MyAppointmentsPage() {
  const [status, setStatus] = useState('');
  const [appointments, setAppointments] = useState([]);
  const [pageInfo, setPageInfo] = useState({ totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function loadAppointments(nextStatus = status) {
    setLoading(true);
    setError('');
    try {
      const response = await fetchAppointments({ status: nextStatus || undefined, size: 50 });
      setAppointments(response.data?.content ?? []);
      setPageInfo(response.data ?? { totalElements: 0 });
    } catch (err) {
      setError(err.message ?? 'Unable to load appointments');
      setAppointments([]);
      setPageInfo({ totalElements: 0 });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAppointments();
  }, []);

  function applyFilters(event) {
    event.preventDefault();
    loadAppointments(status);
  }

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Appointments</p>
          <h1 className={styles.title}>My appointments</h1>
          <p className={styles.subtitle}>{pageInfo.totalElements ?? 0} appointments found</p>
        </div>
        <Link className={styles.primaryButton} to="/doctors">
          Find doctors
        </Link>
      </div>

      {error && <div className={styles.alert}>{error}</div>}

      <form className={styles.filters} onSubmit={applyFilters}>
        <label className={styles.field}>
          <span>Status</span>
          <select value={status} onChange={(event) => setStatus(event.target.value)} disabled={loading}>
            {STATUSES.map((item) => (
              <option key={item || 'ALL'} value={item}>
                {item || 'ALL'}
              </option>
            ))}
          </select>
        </label>
        <button className={styles.primaryButton} type="submit" disabled={loading}>
          Apply
        </button>
        <button className={styles.secondaryButton} type="button" onClick={() => loadAppointments(status)} disabled={loading}>
          Refresh
        </button>
      </form>

      <section className={styles.panel}>
        <div className={styles.panelHeader}>
          <h2>Results</h2>
          <span>{appointments.length} shown</span>
        </div>

        {loading ? (
          <p className={styles.empty}>Loading appointments...</p>
        ) : appointments.length === 0 ? (
          <p className={styles.empty}>No appointments match the current filter.</p>
        ) : (
          <div className={styles.list}>
            {appointments.map((appointment) => (
              <article className={styles.item} key={appointment.id}>
                <div className={styles.itemTop}>
                  <div className={styles.itemTitle}>
                    <strong>{formatDateTime(appointment.scheduledStart)}</strong>
                    <span className={styles.meta}>Doctor {appointment.doctorId}</span>
                  </div>
                  <span className={`${styles.badge} ${statusClass(appointment.status)}`}>{appointment.status}</span>
                </div>
                <p>{appointment.reason || 'No reason provided.'}</p>
                <div className={styles.rowActions}>
                  <span className={styles.meta}>{appointment.id}</span>
                  <Link className={styles.linkButton} to={`/appointments/${appointment.id}`}>
                    Details
                  </Link>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  );
}
