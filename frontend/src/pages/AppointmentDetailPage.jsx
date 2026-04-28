import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { cancelAppointment, fetchAppointment } from '../services/appointmentService.js';
import styles from './Phase3Pages.module.css';

const STATUS_STEPS = ['PENDING', 'CONFIRMED', 'COMPLETED'];

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

function canCancel(status) {
  return status === 'PENDING' || status === 'CONFIRMED';
}

export default function AppointmentDetailPage() {
  const { appointmentId } = useParams();
  const [appointment, setAppointment] = useState(null);
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  async function loadAppointment() {
    setLoading(true);
    setError('');
    try {
      const response = await fetchAppointment(appointmentId);
      setAppointment(response.data);
    } catch (err) {
      setError(err.message ?? 'Unable to load appointment');
      setAppointment(null);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAppointment();
  }, [appointmentId]);

  async function submitCancel(event) {
    event.preventDefault();
    if (!appointment || !canCancel(appointment.status)) return;
    setSaving(true);
    setError('');
    setMessage('');
    try {
      const response = await cancelAppointment(appointment.id, reason);
      setAppointment(response.data);
      setReason('');
      setMessage('Appointment cancelled.');
    } catch (err) {
      setError(err.message ?? 'Unable to cancel appointment');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Appointment</p>
          <h1 className={styles.title}>Appointment detail</h1>
          <p className={styles.subtitle}>{appointmentId}</p>
        </div>
        <Link className={styles.secondaryButton} to="/appointments">
          Back to list
        </Link>
      </div>

      {error && <div className={styles.alert}>{error}</div>}
      {message && <div className={styles.success}>{message}</div>}

      {loading ? (
        <section className={styles.panel}>
          <p className={styles.empty}>Loading appointment...</p>
        </section>
      ) : !appointment ? (
        <section className={styles.panel}>
          <p className={styles.empty}>Appointment not found.</p>
        </section>
      ) : (
        <div className={styles.layout}>
          <section className={styles.panel}>
            <div className={styles.panelHeader}>
              <h2>Summary</h2>
              <span className={`${styles.badge} ${statusClass(appointment.status)}`}>{appointment.status}</span>
            </div>

            <dl className={styles.detailGrid}>
              <div className={styles.detailBox}>
                <dt>Start</dt>
                <dd>{formatDateTime(appointment.scheduledStart)}</dd>
              </div>
              <div className={styles.detailBox}>
                <dt>End</dt>
                <dd>{formatDateTime(appointment.scheduledEnd)}</dd>
              </div>
              <div className={styles.detailBox}>
                <dt>Doctor ID</dt>
                <dd>{appointment.doctorId}</dd>
              </div>
              <div className={styles.detailBox}>
                <dt>Slot ID</dt>
                <dd>{appointment.slotId ?? '-'}</dd>
              </div>
              <div className={styles.detailBox}>
                <dt>Reason</dt>
                <dd>{appointment.reason || '-'}</dd>
              </div>
              <div className={styles.detailBox}>
                <dt>Cancellation reason</dt>
                <dd>{appointment.cancellationReason || '-'}</dd>
              </div>
            </dl>
          </section>

          <aside className={styles.panel}>
            <div className={styles.panelHeader}>
              <h2>Status</h2>
            </div>
            <div className={styles.timeline}>
              {STATUS_STEPS.map((step) => (
                <div className={styles.timelineStep} key={step}>
                  <span className={`${styles.dot} ${appointment.status === step ? styles.activeDot : ''}`} />
                  <span>{step}</span>
                </div>
              ))}
              {(appointment.status === 'CANCELLED' || appointment.status === 'REJECTED') && (
                <div className={styles.timelineStep}>
                  <span className={`${styles.dot} ${styles.activeDot}`} />
                  <span>{appointment.status}</span>
                </div>
              )}
            </div>

            <form className={styles.panel} onSubmit={submitCancel}>
              <label className={styles.field}>
                <span>Cancel reason</span>
                <textarea
                  value={reason}
                  onChange={(event) => setReason(event.target.value)}
                  maxLength={500}
                  disabled={!canCancel(appointment.status) || saving}
                />
              </label>
              <button className={styles.dangerButton} type="submit" disabled={!canCancel(appointment.status) || saving}>
                Cancel appointment
              </button>
            </form>
          </aside>
        </div>
      )}
    </section>
  );
}
