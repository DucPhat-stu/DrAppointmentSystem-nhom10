import { useEffect, useMemo, useState } from 'react';
import { fetchAdminDoctors } from '../../services/adminService.js';
import styles from './AdminPage.module.css';

export default function AdminDoctorsPage() {
  const [filters, setFilters]   = useState({ page: 0, size: 20 });
  const [pageData, setPageData] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState('');

  const canPrev = filters.page > 0;
  const canNext = useMemo(() => pageData.totalPages > 0 && filters.page < pageData.totalPages - 1, [filters.page, pageData.totalPages]);

  async function load(f = filters) {
    setLoading(true); setError('');
    try {
      const res = await fetchAdminDoctors(f);
      setPageData(res.data ?? { content: [], totalElements: 0, totalPages: 0 });
    } catch (e) { setError(e.message ?? 'Cannot load doctors'); }
    finally { setLoading(false); }
  }

  useEffect(() => { load(); }, []);

  async function movePage(d) {
    const next = { ...filters, page: filters.page + d };
    setFilters(next); await load(next);
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>Quản lý Bác sĩ</h1>
          <p className={styles.pageSub}>{pageData.totalElements} bác sĩ trong hệ thống</p>
        </div>
        <button className={styles.btnSecondary} type="button" onClick={() => load()} disabled={loading}>Refresh</button>
      </div>

      {error && <div className={styles.alert + ' ' + styles.alertError}>{error}</div>}

      <div className={styles.tableCard}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Tên bác sĩ</th>
              <th>Email</th>
              <th>Chuyên khoa</th>
              <th>Khoa</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={5} className={styles.emptyCell}>Đang tải...</td></tr>
            ) : pageData.content.length === 0 ? (
              <tr><td colSpan={5} className={styles.emptyCell}>Không có bác sĩ nào.</td></tr>
            ) : pageData.content.map(d => (
              <tr key={d.userId}>
                <td className={styles.idCell} title={d.userId}>{d.userId?.substring(0, 8)}...</td>
                <td className={styles.nameCell}>{d.fullName || '—'}</td>
                <td>{d.email || '—'}</td>
                <td><span className={`${styles.badge} ${styles.badgeTeal}`}>{d.specialty || '—'}</span></td>
                <td>{d.department || '—'}</td>
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

      <div className={styles.infoNote}>
        💡 Để vô hiệu hoá tài khoản bác sĩ, vào <strong>Quản lý Người dùng</strong> và disable tài khoản tương ứng.
      </div>
    </div>
  );
}
