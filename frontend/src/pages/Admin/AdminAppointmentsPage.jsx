import { useEffect, useMemo, useState } from 'react';
import { fetchAdminAppointments, adminCancelAppointment } from '../../services/adminService.js';
import styles from './AdminPage.module.css';

const STATUSES = ['', 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'REJECTED'];

const STATUS_MAP = {
  PENDING:   styles.badgeYellow,
  CONFIRMED: styles.badgeBlue,
  COMPLETED: styles.badgeGreen,
  CANCELLED: styles.badgeRed,
  REJECTED:  styles.badgeOrange,
};

function fmt(val) {
  if (!val) return '—';
  return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(val));
}

export default function AdminAppointmentsPage() {
  const [filters, setFilters]   = useState({ status: '', page: 0, size: 20 });
  const [pageData, setPageData] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading]   = useState(true);
  const [cancelTarget, setCancelTarget] = useState(null);
  const [cancelReason, setCancelReason] = useState('');
  const [savingId, setSavingId] = useState(null);
  const [message, setMessage]   = useState('');
  const [error, setError]       = useState('');

  const canPrev = filters.page > 0;
  const canNext = useMemo(() => pageData.totalPages > 0 && filters.page < pageData.totalPages - 1, [filters.page, pageData.totalPages]);

  async function load(f = filters) {
    setLoading(true); setError(''); setMessage('');
    try {
      const res = await fetchAdminAppointments(f);
      setPageData(res.data ?? { content: [], totalElements: 0, totalPages: 0 });
    } catch (e) { setError(e.message ?? 'Cannot load appointments'); }
    finally { setLoading(false); }
  }

  useEffect(() => { load(); }, []);

  async function apply(e) {
    e.preventDefault();
    const next = { ...filters, page: 0 };
    setFilters(next); await load(next);
  }

  async function movePage(d) {
    const next = { ...filters, page: filters.page + d };
    setFilters(next); await load(next);
  }

  async function confirmCancel(e) {
    e.preventDefault();
    if (!cancelTarget) return;
    setSavingId(cancelTarget.id);
    setError('');
    try {
      await adminCancelAppointment(cancelTarget.id, cancelReason);
      setMessage(`Đã huỷ lịch hẹn ${cancelTarget.id.substring(0, 8)}`);
      setCancelTarget(null); setCancelReason('');
      await load();
    } catch (e) { setError(e.message ?? 'Cancel failed'); }
    finally { setSavingId(null); }
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>Quản lý Lịch hẹn</h1>
          <p className={styles.pageSub}>{pageData.totalElements} lịch hẹn tổng cộng</p>
        </div>
      </div>

      {message && <div className={styles.alert + ' ' + styles.alertSuccess}>{message}</div>}
      {error   && <div className={styles.alert + ' ' + styles.alertError}>{error}</div>}

      <form className={styles.filterBar} onSubmit={apply}>
        <label className={styles.filterItem}>
          <span>Trạng thái</span>
          <select value={filters.status} onChange={e => setFilters(f => ({ ...f, status: e.target.value }))}>
            {STATUSES.map(s => <option key={s || 'ALL'} value={s}>{s || 'Tất cả'}</option>)}
          </select>
        </label>
        <button className={styles.btnPrimary} type="submit" disabled={loading}>Lọc</button>
        <button className={styles.btnSecondary} type="button" onClick={() => load()} disabled={loading}>Refresh</button>
      </form>

      <div className={styles.tableCard}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Bệnh nhân</th>
              <th>Bác sĩ</th>
              <th>Bắt đầu</th>
              <th>Kết thúc</th>
              <th>Trạng thái</th>
              <th>Huỷ</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={7} className={styles.emptyCell}>Đang tải...</td></tr>
            ) : pageData.content.length === 0 ? (
              <tr><td colSpan={7} className={styles.emptyCell}>Không có lịch hẹn.</td></tr>
            ) : pageData.content.map(a => (
              <tr key={a.id}>
                <td className={styles.idCell} title={a.id}>{a.id?.substring(0, 8)}...</td>
                <td className={styles.idCell} title={a.patientId}>{a.patientId?.substring(0, 8)}...</td>
                <td className={styles.idCell} title={a.doctorId}>{a.doctorId?.substring(0, 8)}...</td>
                <td className={styles.dateCell}>{fmt(a.scheduledStart)}</td>
                <td className={styles.dateCell}>{fmt(a.scheduledEnd)}</td>
                <td><span className={`${styles.badge} ${STATUS_MAP[a.status] ?? ''}`}>{a.status}</span></td>
                <td>
                  {(a.status === 'PENDING' || a.status === 'CONFIRMED') && (
                    <button
                      className={styles.btnDanger}
                      type="button"
                      disabled={savingId === a.id}
                      onClick={() => { setCancelTarget(a); setCancelReason(''); }}
                    >
                      Huỷ
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className={styles.pager}>
          <button className={styles.btnSecondary} type="button" disabled={!canPrev || loading} onClick={() => movePage(-1)}>← Trước</button>
          <span className={styles.pageInfo}>Trang {pageData.totalPages === 0 ? 0 : filters.page + 1} / {pageData.totalPages}</span>
          <button className={styles.btnSecondary} type="button" disabled={!canNext || loading} onClick={() => movePage(1)}>Sau →</button>
        </div>
      </div>

      {/* Cancel modal */}
      {cancelTarget && (
        <div className={styles.modalBackdrop}>
          <form className={styles.modal} onSubmit={confirmCancel}>
            <h2 className={styles.modalTitle}>Huỷ lịch hẹn</h2>
            <p className={styles.modalSub}>ID: <code>{cancelTarget.id}</code></p>
            <p className={styles.modalSub}>Bắt đầu: {fmt(cancelTarget.scheduledStart)}</p>
            <label className={styles.modalLabel}>
              <span>Lý do huỷ</span>
              <textarea
                value={cancelReason}
                onChange={e => setCancelReason(e.target.value)}
                rows={3}
                placeholder="Nhập lý do..."
                maxLength={500}
              />
            </label>
            <div className={styles.modalActions}>
              <button className={styles.btnSecondary} type="button" onClick={() => setCancelTarget(null)}>Đóng</button>
              <button className={styles.btnDanger} type="submit" disabled={!!savingId}>Xác nhận huỷ</button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
