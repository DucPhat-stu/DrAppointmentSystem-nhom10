import { useEffect, useMemo, useState } from 'react';
import { fetchAdminUsers, disableUser, enableUser } from '../../services/adminService.js';
import styles from './AdminPage.module.css';

const ROLES   = ['', 'PATIENT', 'DOCTOR', 'ADMIN'];
const STATUSES = ['', 'ACTIVE', 'INACTIVE', 'PENDING', 'LOCKED'];

function StatusBadge({ status }) {
  const map = {
    ACTIVE:   styles.badgeGreen,
    INACTIVE: styles.badgeRed,
    PENDING:  styles.badgeYellow,
    LOCKED:   styles.badgeOrange,
  };
  return <span className={`${styles.badge} ${map[status] ?? ''}`}>{status}</span>;
}

function RoleBadge({ role }) {
  const map = { PATIENT: styles.badgeBlue, DOCTOR: styles.badgeTeal, ADMIN: styles.badgePurple, SUPER_ADMIN: styles.badgePurple };
  return <span className={`${styles.badge} ${map[role] ?? ''}`}>{role}</span>;
}

export default function AdminUsersPage() {
  const [filters, setFilters]   = useState({ role: '', status: '', q: '', page: 0, size: 20 });
  const [pageData, setPageData] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading]   = useState(true);
  const [savingId, setSavingId] = useState(null);
  const [message, setMessage]   = useState('');
  const [error, setError]       = useState('');

  const canPrev = filters.page > 0;
  const canNext = useMemo(() => pageData.totalPages > 0 && filters.page < pageData.totalPages - 1, [filters.page, pageData.totalPages]);

  async function load(f = filters) {
    setLoading(true); setError(''); setMessage('');
    try {
      const res = await fetchAdminUsers(f);
      setPageData(res.data ?? { content: [], totalElements: 0, totalPages: 0 });
    } catch (e) { setError(e.message ?? 'Cannot load users'); }
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

  async function toggleUser(user) {
    setSavingId(user.id);
    setError(''); setMessage('');
    try {
      if (user.status === 'ACTIVE') {
        await disableUser(user.id);
        setMessage(`Đã vô hiệu hoá ${user.email}`);
      } else {
        await enableUser(user.id);
        setMessage(`Đã kích hoạt lại ${user.email}`);
      }
      await load();
    } catch (e) { setError(e.message ?? 'Action failed'); }
    finally { setSavingId(null); }
  }

  function formatDate(val) {
    if (!val) return '—';
    return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(val));
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>Quản lý Người dùng</h1>
          <p className={styles.pageSub}>{pageData.totalElements} tổng cộng</p>
        </div>
      </div>

      {message && <div className={styles.alert + ' ' + styles.alertSuccess}>{message}</div>}
      {error   && <div className={styles.alert + ' ' + styles.alertError}>{error}</div>}

      {/* Filters */}
      <form className={styles.filterBar} onSubmit={apply}>
        <label className={styles.filterItem}>
          <span>Tìm kiếm</span>
          <input
            value={filters.q}
            onChange={e => setFilters(f => ({ ...f, q: e.target.value }))}
            placeholder="Tên, email hoặc SĐT"
          />
        </label>
        <label className={styles.filterItem}>
          <span>Role</span>
          <select value={filters.role} onChange={e => setFilters(f => ({ ...f, role: e.target.value }))}>
            {ROLES.map(r => <option key={r || 'ALL'} value={r}>{r || 'Tất cả'}</option>)}
          </select>
        </label>
        <label className={styles.filterItem}>
          <span>Trạng thái</span>
          <select value={filters.status} onChange={e => setFilters(f => ({ ...f, status: e.target.value }))}>
            {STATUSES.map(s => <option key={s || 'ALL'} value={s}>{s || 'Tất cả'}</option>)}
          </select>
        </label>
        <button className={styles.btnPrimary} type="submit" disabled={loading}>Lọc</button>
        <button className={styles.btnSecondary} type="button" onClick={() => load()} disabled={loading}>Refresh</button>
      </form>

      {/* Table */}
      <div className={styles.tableCard}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Email</th>
              <th>Tên</th>
              <th>Role</th>
              <th>Trạng thái</th>
              <th>Đăng nhập lần cuối</th>
              <th>Ngày tạo</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={7} className={styles.emptyCell}>Đang tải...</td></tr>
            ) : pageData.content.length === 0 ? (
              <tr><td colSpan={7} className={styles.emptyCell}>Không có dữ liệu.</td></tr>
            ) : pageData.content.map(u => (
              <tr key={u.id}>
                <td className={styles.emailCell}>{u.email}</td>
                <td>{u.fullName || '—'}</td>
                <td><RoleBadge role={u.role} /></td>
                <td><StatusBadge status={u.status} /></td>
                <td className={styles.dateCell}>{formatDate(u.lastLoginAt)}</td>
                <td className={styles.dateCell}>{formatDate(u.createdAt)}</td>
                <td>
                  {u.role !== 'ADMIN' && u.role !== 'SUPER_ADMIN' && (
                    <button
                      className={u.status === 'ACTIVE' ? styles.btnDanger : styles.btnSuccess}
                      type="button"
                      disabled={savingId === u.id}
                      onClick={() => toggleUser(u)}
                    >
                      {savingId === u.id ? '...' : u.status === 'ACTIVE' ? 'Vô hiệu hoá' : 'Kích hoạt'}
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
    </div>
  );
}
