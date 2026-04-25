import { useEffect, useMemo, useState } from 'react';
import {
  cancelDoctorAppointment,
  confirmDoctorAppointment,
  fetchDoctorAppointment,
  fetchDoctorAppointments,
  rejectDoctorAppointment,
} from '../services/doctorService.js';
import styles from './DoctorAppointmentDashboardPage.module.css';

const statuses = ['', 'PENDING', 'CONFIRMED', 'CANCELLED', 'REJECTED', 'COMPLETED'];
const actionLabels = {
  confirm: 'Confirm appointment',
  reject: 'Reject appointment',
  cancel: 'Cancel appointment',
};

function today() {
  return new Date().toISOString().slice(0, 10);
}

function formatDateTime(value) {
  if (!value) return '-';
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function idempotencyKey() {
  if (window.crypto?.randomUUID) {
    return window.crypto.randomUUID();
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export default function DoctorAppointmentDashboardPage() {
  const [filters, setFilters] = useState({ date: today(), status: '', page: 0, size: 10 });
  const [pageData, setPageData] = useState({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  const [selected, setSelected] = useState(null);
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [modal, setModal] = useState(null);
  const [reason, setReason] = useState('');

  const canPrevious = filters.page > 0;
  const canNext = useMemo(
    () => pageData.totalPages > 0 && filters.page < pageData.totalPages - 1,
    [filters.page, pageData.totalPages],
  );

  async function loadAppointments(nextFilters = filters) {
    setLoading(true);
    setError('');
    try {
      const response = await fetchDoctorAppointments(nextFilters);
      const data = response.data ?? { content: [], page: 0, size: nextFilters.size, totalElements: 0, totalPages: 0 };
      setPageData(data);
      if (data.content.length > 0) {
        setSelected((current) => data.content.find((item) => item.id === current?.id) ?? data.content[0]);
      } else {
        setSelected(null);
      }
    } catch (err) {
      setError(err.message ?? 'Unable to load appointments');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAppointments();
  }, []);

  async function applyFilters(event) {
    event.preventDefault();
    const next = { ...filters, page: 0 };
    setFilters(next);
    await loadAppointments(next);
  }

  async function selectAppointment(appointment) {
    setSelected(appointment);
    setDetailLoading(true);
    setError('');
    try {
      const response = await fetchDoctorAppointment(appointment.id);
      setSelected(response.data);
    } catch (err) {
      setError(err.message ?? 'Unable to load appointment detail');
    } finally {
      setDetailLoading(false);
    }
  }

  async function movePage(direction) {
    const next = { ...filters, page: filters.page + direction };
    setFilters(next);
    await loadAppointments(next);
  }

  function openAction(action, appointment = selected) {
    setModal({ action, appointment });
    setReason('');
  }

  function mergeUpdatedAppointment(updated) {
    setSelected(updated);
    setPageData((current) => ({
      ...current,
      content: current.content.map((item) => (item.id === updated.id ? updated : item)),
    }));
  }

  async function submitAction(event) {
    event.preventDefault();
    if (!modal?.appointment) return;

    setActionLoading(true);
    setError('');
    try {
      const key = idempotencyKey();
      let response;
      if (modal.action === 'confirm') {
        response = await confirmDoctorAppointment(modal.appointment.id, key);
      } else if (modal.action === 'reject') {
        response = await rejectDoctorAppointment(modal.appointment.id, key, reason);
      } else {
        response = await cancelDoctorAppointment(modal.appointment.id, key, reason);
      }
      mergeUpdatedAppointment(response.data);
      setModal(null);
      setReason('');
    } catch (err) {
      setError(err.message ?? 'Unable to update appointment');
    } finally {
      setActionLoading(false);
    }
  }

  function actionSet(appointment) {
    if (!appointment) return [];
    if (appointment.status === 'PENDING') return ['confirm', 'reject', 'cancel'];
    if (appointment.status === 'CONFIRMED') return ['cancel'];
    return [];
  }

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Doctor Appointments</p>
          <h1 className={styles.title}>Appointment dashboard</h1>
        </div>
        <button className={styles.secondaryButton} type="button" onClick={() => loadAppointments()} disabled={loading || actionLoading}>
          Refresh
        </button>
      </div>

      {error && <div className={styles.alert}>{error}</div>}

      <form className={styles.filters} onSubmit={applyFilters}>
        <label>
          <span>Date</span>
          <input
            type="date"
            value={filters.date}
            onChange={(event) => setFilters((current) => ({ ...current, date: event.target.value }))}
          />
        </label>
        <label>
          <span>Status</span>
          <select
            value={filters.status}
            onChange={(event) => setFilters((current) => ({ ...current, status: event.target.value }))}
          >
            {statuses.map((status) => (
              <option key={status || 'ALL'} value={status}>{status || 'ALL'}</option>
            ))}
          </select>
        </label>
        <button className={styles.primaryButton} type="submit" disabled={loading}>Apply</button>
      </form>

      <div className={styles.layout}>
        <div className={styles.list}>
          <div className={styles.listHeader}>
            <h2>Appointments</h2>
            <span>{pageData.totalElements} total</span>
          </div>

          {loading ? (
            <p className={styles.empty}>Loading appointments...</p>
          ) : pageData.content.length === 0 ? (
            <p className={styles.empty}>No appointments match the current filters.</p>
          ) : (
            pageData.content.map((appointment) => (
              <button
                type="button"
                key={appointment.id}
                className={`${styles.appointmentRow} ${selected?.id === appointment.id ? styles.selectedRow : ''}`}
                onClick={() => selectAppointment(appointment)}
              >
                <span>
                  <strong>{formatDateTime(appointment.scheduledStart)}</strong>
                  <small>Patient {appointment.patientId}</small>
                </span>
                <em className={`${styles.status} ${styles[appointment.status?.toLowerCase()] ?? ''}`}>{appointment.status}</em>
              </button>
            ))
          )}

          <div className={styles.pager}>
            <button className={styles.secondaryButton} type="button" onClick={() => movePage(-1)} disabled={!canPrevious || loading}>
              Previous
            </button>
            <span>Page {pageData.totalPages === 0 ? 0 : filters.page + 1} / {pageData.totalPages}</span>
            <button className={styles.secondaryButton} type="button" onClick={() => movePage(1)} disabled={!canNext || loading}>
              Next
            </button>
          </div>
        </div>

        <aside className={styles.detail}>
          <div className={styles.listHeader}>
            <h2>Detail</h2>
            {detailLoading && <span>Loading...</span>}
          </div>

          {selected ? (
            <>
              <dl className={styles.detailGrid}>
                <div>
                  <dt>Status</dt>
                  <dd>{selected.status}</dd>
                </div>
                <div>
                  <dt>Time</dt>
                  <dd>{formatDateTime(selected.scheduledStart)} - {formatDateTime(selected.scheduledEnd)}</dd>
                </div>
                <div>
                  <dt>Patient ID</dt>
                  <dd>{selected.patientId}</dd>
                </div>
                <div>
                  <dt>Slot ID</dt>
                  <dd>{selected.slotId ?? '-'}</dd>
                </div>
                <div>
                  <dt>Reason</dt>
                  <dd>{selected.reason || '-'}</dd>
                </div>
                <div>
                  <dt>Resolution</dt>
                  <dd>{selected.cancellationReason || '-'}</dd>
                </div>
              </dl>
              <div className={styles.actions}>
                {actionSet(selected).map((action) => (
                  <button
                    key={action}
                    className={action === 'confirm' ? styles.primaryButton : styles.dangerButton}
                    type="button"
                    onClick={() => openAction(action)}
                    disabled={actionLoading}
                  >
                    {actionLabels[action]}
                  </button>
                ))}
              </div>
            </>
          ) : (
            <p className={styles.empty}>Select an appointment to view details.</p>
          )}
        </aside>
      </div>

      {modal && (
        <div className={styles.modalBackdrop} role="presentation">
          <form className={styles.modal} onSubmit={submitAction}>
            <h2>{actionLabels[modal.action]}</h2>
            <p>{formatDateTime(modal.appointment.scheduledStart)} for patient {modal.appointment.patientId}</p>
            {modal.action !== 'confirm' && (
              <label>
                <span>Reason</span>
                <textarea
                  value={reason}
                  onChange={(event) => setReason(event.target.value)}
                  maxLength={500}
                  rows={4}
                />
              </label>
            )}
            <div className={styles.modalActions}>
              <button className={styles.secondaryButton} type="button" onClick={() => setModal(null)} disabled={actionLoading}>
                Close
              </button>
              <button className={modal.action === 'confirm' ? styles.primaryButton : styles.dangerButton} type="submit" disabled={actionLoading}>
                Confirm
              </button>
            </div>
          </form>
        </div>
      )}
    </section>
  );
}
