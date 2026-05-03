import { useEffect, useMemo, useState } from 'react';
import { disableUser, enableUser, fetchAdminUsers } from '../../services/adminService.js';
import styles from './AdminPage.module.css';

const ROLES = ['', 'PATIENT', 'DOCTOR', 'ADMIN'];
const STATUSES = ['', 'ACTIVE', 'INACTIVE', 'PENDING', 'LOCKED'];

function StatusBadge({ status }) {
  const map = {
    ACTIVE: styles.badgeGreen,
    INACTIVE: styles.badgeRed,
    PENDING: styles.badgeYellow,
    LOCKED: styles.badgeOrange,
  };
  return <span className={`${styles.badge} ${map[status] ?? ''}`}>{status}</span>;
}

function RoleBadge({ role }) {
  const map = { PATIENT: styles.badgeBlue, DOCTOR: styles.badgeTeal, ADMIN: styles.badgePurple, SUPER_ADMIN: styles.badgePurple };
  return <span className={`${styles.badge} ${map[role] ?? ''}`}>{role}</span>;
}

export default function AdminUsersPage() {
  const [filters, setFilters] = useState({ role: '', status: '', q: '', page: 0, size: 20 });
  const [pageData, setPageData] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
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
      const response = await fetchAdminUsers(nextFilters);
      setPageData(response.data ?? { content: [], totalElements: 0, totalPages: 0 });
    } catch (err) {
      setError(err.message ?? 'Cannot load users');
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

  async function toggleUser(user) {
    setSavingId(user.id);
    setError('');
    setMessage('');
    try {
      if (user.status === 'ACTIVE') {
        await disableUser(user.id);
        setMessage(`${user.email} was disabled`);
      } else {
        await enableUser(user.id);
        setMessage(`${user.email} was enabled`);
      }
      await load();
    } catch (err) {
      setError(err.message ?? 'Action failed');
    } finally {
      setSavingId(null);
    }
  }

  function formatDate(value) {
    if (!value) return '-';
    return new Intl.DateTimeFormat('en-US', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>User Management</h1>
          <p className={styles.pageSub}>{pageData.totalElements} total users</p>
        </div>
      </div>

      {message && <div className={styles.alert + ' ' + styles.alertSuccess}>{message}</div>}
      {error && <div className={styles.alert + ' ' + styles.alertError}>{error}</div>}

      <form className={styles.filterBar} onSubmit={apply}>
        <label className={styles.filterItem}>
          <span>Search</span>
          <input
            value={filters.q}
            onChange={(event) => setFilters((current) => ({ ...current, q: event.target.value }))}
            placeholder="Name, email, or phone"
          />
        </label>
        <label className={styles.filterItem}>
          <span>Role</span>
          <select value={filters.role} onChange={(event) => setFilters((current) => ({ ...current, role: event.target.value }))}>
            {ROLES.map((role) => <option key={role || 'ALL'} value={role}>{role || 'All'}</option>)}
          </select>
        </label>
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
              <th>Email</th>
              <th>Name</th>
              <th>Role</th>
              <th>Status</th>
              <th>Last login</th>
              <th>Created</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={7} className={styles.emptyCell}>Loading...</td></tr>
            ) : pageData.content.length === 0 ? (
              <tr><td colSpan={7} className={styles.emptyCell}>No users found.</td></tr>
            ) : pageData.content.map((user) => (
              <tr key={user.id}>
                <td className={styles.emailCell}>{user.email}</td>
                <td>{user.fullName || '-'}</td>
                <td><RoleBadge role={user.role} /></td>
                <td><StatusBadge status={user.status} /></td>
                <td className={styles.dateCell}>{formatDate(user.lastLoginAt)}</td>
                <td className={styles.dateCell}>{formatDate(user.createdAt)}</td>
                <td>
                  {user.role !== 'ADMIN' && user.role !== 'SUPER_ADMIN' && (
                    <button
                      className={user.status === 'ACTIVE' ? styles.btnDanger : styles.btnSuccess}
                      type="button"
                      disabled={savingId === user.id}
                      onClick={() => toggleUser(user)}
                    >
                      {savingId === user.id ? '...' : user.status === 'ACTIVE' ? 'Disable' : 'Enable'}
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
    </div>
  );
}
