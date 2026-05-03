import { useEffect, useMemo, useState } from 'react';
import { fetchAdminDoctors } from '../../services/adminService.js';
import styles from './AdminPage.module.css';

export default function AdminDoctorsPage() {
  const [filters, setFilters] = useState({ page: 0, size: 20 });
  const [pageData, setPageData] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const canPrev = filters.page > 0;
  const canNext = useMemo(() => pageData.totalPages > 0 && filters.page < pageData.totalPages - 1, [filters.page, pageData.totalPages]);

  async function load(nextFilters = filters) {
    setLoading(true);
    setError('');
    try {
      const response = await fetchAdminDoctors(nextFilters);
      setPageData(response.data ?? { content: [], totalElements: 0, totalPages: 0 });
    } catch (err) {
      setError(err.message ?? 'Cannot load doctors');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function movePage(delta) {
    const next = { ...filters, page: filters.page + delta };
    setFilters(next);
    await load(next);
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>Doctor Management</h1>
          <p className={styles.pageSub}>{pageData.totalElements} doctors in the system</p>
        </div>
        <button className={styles.btnSecondary} type="button" onClick={() => load()} disabled={loading}>Refresh</button>
      </div>

      {error && <div className={styles.alert + ' ' + styles.alertError}>{error}</div>}

      <div className={styles.tableCard}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Doctor name</th>
              <th>Email</th>
              <th>Specialty</th>
              <th>Department</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={5} className={styles.emptyCell}>Loading...</td></tr>
            ) : pageData.content.length === 0 ? (
              <tr><td colSpan={5} className={styles.emptyCell}>No doctors found.</td></tr>
            ) : pageData.content.map((doctor) => (
              <tr key={doctor.userId}>
                <td className={styles.idCell} title={doctor.userId}>{doctor.userId?.substring(0, 8)}...</td>
                <td className={styles.nameCell}>{doctor.fullName || '-'}</td>
                <td>{doctor.email || '-'}</td>
                <td><span className={`${styles.badge} ${styles.badgeTeal}`}>{doctor.specialty || '-'}</span></td>
                <td>{doctor.department || '-'}</td>
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

      <div className={styles.infoNote}>
        To disable a doctor account, open <strong>User Management</strong> and disable the corresponding user.
      </div>
    </div>
  );
}
