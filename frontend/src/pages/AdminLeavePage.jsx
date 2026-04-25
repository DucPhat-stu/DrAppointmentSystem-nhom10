import { useEffect, useMemo, useState } from 'react';
import {
  approveDoctorLeave,
  fetchAdminLeaves,
  rejectDoctorLeave,
} from '../services/doctorService.js';
import styles from './DoctorLeavePage.module.css';

const statuses = ['', 'PENDING', 'APPROVED', 'REJECTED'];

function formatDate(value) {
  if (!value) return '-';
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(`${value}T00:00:00`));
}

export default function AdminLeavePage() {
  const [filters, setFilters] = useState({ status: 'PENDING', page: 0, size: 10 });
  const [pageData, setPageData] = useState({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [rejecting, setRejecting] = useState(null);
  const [reason, setReason] = useState('');

  const canPrevious = filters.page > 0;
  const canNext = useMemo(
    () => pageData.totalPages > 0 && filters.page < pageData.totalPages - 1,
    [filters.page, pageData.totalPages],
  );

  async function loadLeaves(nextFilters = filters) {
    setLoading(true);
    setError('');
    try {
      const response = await fetchAdminLeaves(nextFilters);
      setPageData(response.data ?? { content: [], page: 0, size: nextFilters.size, totalElements: 0, totalPages: 0 });
    } catch (err) {
      setError(err.message ?? 'Unable to load leave requests');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadLeaves();
  }, []);

  async function applyFilters(event) {
    event.preventDefault();
    const next = { ...filters, page: 0 };
    setFilters(next);
    await loadLeaves(next);
  }

  async function movePage(direction) {
    const next = { ...filters, page: filters.page + direction };
    setFilters(next);
    await loadLeaves(next);
  }

  async function approve(leaveId) {
    setSavingId(leaveId);
    setError('');
    setMessage('');
    try {
      await approveDoctorLeave(leaveId);
      setMessage('Leave request approved.');
      await loadLeaves();
    } catch (err) {
      setError(err.message ?? 'Unable to approve leave request');
    } finally {
      setSavingId(null);
    }
  }

  async function submitReject(event) {
    event.preventDefault();
    if (!rejecting) return;

    setSavingId(rejecting.id);
    setError('');
    setMessage('');
    try {
      await rejectDoctorLeave(rejecting.id, reason);
      setMessage('Leave request rejected.');
      setRejecting(null);
      setReason('');
      await loadLeaves();
    } catch (err) {
      setError(err.message ?? 'Unable to reject leave request');
    } finally {
      setSavingId(null);
    }
  }

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Admin Leave</p>
          <h1 className={styles.title}>Leave approvals</h1>
        </div>
        <button className={styles.secondaryButton} type="button" onClick={() => loadLeaves()} disabled={loading || !!savingId}>
          Refresh
        </button>
      </div>

      {error && <div className={styles.alert}>{error}</div>}
      {message && <div className={styles.success}>{message}</div>}

      <form className={styles.filters} onSubmit={applyFilters}>
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

      <section className={styles.panel}>
        <div className={styles.panelHeader}>
          <h2>Requests</h2>
          <span>{pageData.totalElements} total</span>
        </div>

        {loading ? (
          <p className={styles.empty}>Loading leave requests...</p>
        ) : pageData.content.length === 0 ? (
          <p className={styles.empty}>No leave requests match the current filter.</p>
        ) : (
          <div className={styles.list}>
            {pageData.content.map((leave) => (
              <article className={styles.adminRow} key={leave.id}>
                <div>
                  <strong>{formatDate(leave.startDate)} - {formatDate(leave.endDate)}</strong>
                  <span>Doctor {leave.doctorId}</span>
                </div>
                <em className={`${styles.status} ${styles[leave.status?.toLowerCase()] ?? ''}`}>{leave.status}</em>
                <div className={styles.actions}>
                  <button
                    className={styles.primaryButton}
                    type="button"
                    onClick={() => approve(leave.id)}
                    disabled={leave.status !== 'PENDING' || savingId === leave.id}
                  >
                    Approve
                  </button>
                  <button
                    className={styles.dangerButton}
                    type="button"
                    onClick={() => {
                      setRejecting(leave);
                      setReason('');
                    }}
                    disabled={leave.status !== 'PENDING' || savingId === leave.id}
                  >
                    Reject
                  </button>
                </div>
              </article>
            ))}
          </div>
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
      </section>

      {rejecting && (
        <div className={styles.modalBackdrop} role="presentation">
          <form className={styles.modal} onSubmit={submitReject}>
            <h2>Reject leave request</h2>
            <p>{formatDate(rejecting.startDate)} - {formatDate(rejecting.endDate)}</p>
            <label>
              <span>Reason</span>
              <textarea
                value={reason}
                onChange={(event) => setReason(event.target.value)}
                maxLength={500}
                rows={4}
              />
            </label>
            <div className={styles.actions}>
              <button className={styles.secondaryButton} type="button" onClick={() => setRejecting(null)} disabled={savingId === rejecting.id}>
                Close
              </button>
              <button className={styles.dangerButton} type="submit" disabled={savingId === rejecting.id}>
                Reject
              </button>
            </div>
          </form>
        </div>
      )}
    </section>
  );
}
