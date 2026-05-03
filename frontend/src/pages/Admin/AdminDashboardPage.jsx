import { useEffect, useState } from 'react';
import { fetchAdminAppointments, fetchAdminDoctors, fetchAdminUsers } from '../../services/adminService.js';
import styles from './AdminDashboardPage.module.css';

const APPOINTMENT_STATUSES = ['PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'REJECTED'];

const STATUS_COLORS = {
  PENDING: { bg: '#fef9c3', text: '#a16207', bar: '#facc15' },
  CONFIRMED: { bg: '#dbeafe', text: '#1e40af', bar: '#3b82f6' },
  COMPLETED: { bg: '#dcfce7', text: '#15803d', bar: '#22c55e' },
  CANCELLED: { bg: '#fee2e2', text: '#dc2626', bar: '#ef4444' },
  REJECTED: { bg: '#fce7f3', text: '#9d174d', bar: '#ec4899' },
};

function KpiCard({ icon, label, value, sub, color, trend }) {
  return (
    <div className={styles.kpiCard} style={{ '--accent': color }}>
      <div className={styles.kpiIcon} style={{ background: `${color}18`, color }}>
        {icon}
      </div>
      <div className={styles.kpiBody}>
        <span className={styles.kpiLabel}>{label}</span>
        <span className={styles.kpiValue}>{value ?? '-'}</span>
        {sub && <span className={styles.kpiSub}>{sub}</span>}
      </div>
      {trend && (
        <span className={`${styles.kpiTrend} ${trend > 0 ? styles.up : styles.down}`}>
          {trend > 0 ? '▲' : '▼'} {Math.abs(trend)}%
        </span>
      )}
    </div>
  );
}

function BarChart({ data, total }) {
  return (
    <div className={styles.barChart}>
      {data.map(({ label, value, color }) => {
        const pct = total > 0 ? Math.round((value / total) * 100) : 0;
        return (
          <div key={label} className={styles.barRow}>
            <span className={styles.barLabel}>{label}</span>
            <div className={styles.barTrack}>
              <div className={styles.barFill} style={{ width: `${pct}%`, background: color }} />
            </div>
            <span className={styles.barValue}>{value}</span>
            <span className={styles.barPct}>({pct}%)</span>
          </div>
        );
      })}
    </div>
  );
}

function DonutChart({ segments, total }) {
  if (total === 0) return <div className={styles.donutEmpty}>No data</div>;
  let offset = 25;
  const radius = 40;
  const circumference = 2 * Math.PI * radius;

  return (
    <div className={styles.donutWrapper}>
      <svg viewBox="0 0 100 100" className={styles.donutSvg}>
        {segments.map(({ label, value, color }) => {
          const pct = value / total;
          const dash = pct * circumference;
          const gap = circumference - dash;
          const segment = (
            <circle
              key={label}
              cx="50"
              cy="50"
              r={radius}
              fill="none"
              stroke={color}
              strokeWidth="18"
              strokeDasharray={`${dash} ${gap}`}
              strokeDashoffset={`${(-offset * circumference) / 100}`}
              transform="rotate(-90 50 50)"
            />
          );
          offset += pct * 100;
          return segment;
        })}
        <text x="50" y="46" textAnchor="middle" className={styles.donutCenter}>{total}</text>
        <text x="50" y="58" textAnchor="middle" className={styles.donutSub}>total</text>
      </svg>
      <div className={styles.donutLegend}>
        {segments.map(({ label, value, color }) => (
          <div key={label} className={styles.legendRow}>
            <span className={styles.legendDot} style={{ background: color }} />
            <span className={styles.legendLabel}>{label}</span>
            <span className={styles.legendVal}>{value}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

export default function AdminDashboardPage() {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadStats() {
      setLoading(true);
      setError('');
      try {
        const [usersRes, doctorsRes, apptRes] = await Promise.allSettled([
          fetchAdminUsers({ size: 1 }),
          fetchAdminDoctors({ size: 1 }),
          fetchAdminAppointments({ size: 100 }),
        ]);

        const totalUsers = usersRes.status === 'fulfilled' ? (usersRes.value?.data?.totalElements ?? 0) : 0;
        const totalDoctors = doctorsRes.status === 'fulfilled' ? (doctorsRes.value?.data?.totalElements ?? 0) : 0;
        const appointmentRows = apptRes.status === 'fulfilled' ? (apptRes.value?.data?.content ?? []) : [];
        const totalAppts = apptRes.status === 'fulfilled' ? (apptRes.value?.data?.totalElements ?? 0) : 0;

        const statusCounts = {};
        APPOINTMENT_STATUSES.forEach((status) => { statusCounts[status] = 0; });
        appointmentRows.forEach((appointment) => {
          if (statusCounts[appointment.status] !== undefined) statusCounts[appointment.status]++;
        });

        const completedCount = statusCounts.COMPLETED;
        const confirmedCount = statusCounts.CONFIRMED;
        const completionRate = totalAppts > 0 ? Math.round((completedCount / totalAppts) * 100) : 0;

        setStats({ totalUsers, totalDoctors, totalAppts, confirmedCount, completionRate, statusCounts });
      } catch {
        setError('Unable to load dashboard data.');
      } finally {
        setLoading(false);
      }
    }
    loadStats();
  }, []);

  const today = new Intl.DateTimeFormat('en-US', { dateStyle: 'full' }).format(new Date());

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>Admin Dashboard</h1>
          <p className={styles.pageDate}>{today}</p>
        </div>
        <span className={styles.liveBadge}>
          <span className={styles.liveDot} />
          Live
        </span>
      </div>

      {error && <div className={styles.errorBanner}>{error}</div>}

      {loading ? (
        <div className={styles.loadingGrid}>
          {[1, 2, 3, 4].map((item) => <div key={item} className={styles.skeleton} />)}
        </div>
      ) : stats && (
        <>
          <div className={styles.kpiGrid}>
            <KpiCard
              icon={<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/></svg>}
              label="Total users"
              value={stats.totalUsers.toLocaleString()}
              sub="Patients + Doctors"
              color="#0ea5e9"
            />
            <KpiCard
              icon={<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>}
              label="Active doctors"
              value={stats.totalDoctors.toLocaleString()}
              sub="Available in the system"
              color="#10b981"
            />
            <KpiCard
              icon={<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>}
              label="Total appointments"
              value={stats.totalAppts.toLocaleString()}
              sub={`${stats.confirmedCount} confirmed`}
              color="#8b5cf6"
            />
            <KpiCard
              icon={<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/><polyline points="16 7 22 7 22 13"/></svg>}
              label="Completion rate"
              value={`${stats.completionRate}%`}
              sub="Completed appointments"
              color="#f59e0b"
            />
          </div>

          <div className={styles.chartsGrid}>
            <div className={styles.chartCard}>
              <div className={styles.chartHeader}>
                <h2 className={styles.chartTitle}>Appointment status distribution</h2>
                <span className={styles.chartTotal}>{stats.totalAppts} total</span>
              </div>
              <DonutChart
                total={stats.totalAppts}
                segments={APPOINTMENT_STATUSES.map((status) => ({
                  label: status,
                  value: stats.statusCounts[status] ?? 0,
                  color: STATUS_COLORS[status].bar,
                }))}
              />
            </div>

            <div className={styles.chartCard}>
              <div className={styles.chartHeader}>
                <h2 className={styles.chartTitle}>Status detail</h2>
              </div>
              <BarChart
                total={stats.totalAppts}
                data={APPOINTMENT_STATUSES.map((status) => ({
                  label: status,
                  value: stats.statusCounts[status] ?? 0,
                  color: STATUS_COLORS[status].bar,
                }))}
              />
            </div>

            <div className={styles.chartCard}>
              <div className={styles.chartHeader}>
                <h2 className={styles.chartTitle}>System status</h2>
              </div>
              <div className={styles.healthList}>
                {[
                  { name: 'auth-service', port: 8086, ok: true },
                  { name: 'user-service', port: 8082, ok: true },
                  { name: 'doctor-service', port: 8083, ok: true },
                  { name: 'appointment-service', port: 8084, ok: true },
                  { name: 'notification-service', port: 8085, ok: true },
                  { name: 'ai-service', port: 8087, ok: null },
                ].map(({ name, port, ok }) => (
                  <div key={name} className={styles.healthRow}>
                    <span className={`${styles.healthDot} ${ok === true ? styles.ok : ok === false ? styles.err : styles.warn}`} />
                    <span className={styles.healthName}>{name}</span>
                    <span className={styles.healthPort}>:{port}</span>
                    <span className={`${styles.healthStatus} ${ok === true ? styles.statusOk : ok === false ? styles.statusErr : styles.statusWarn}`}>
                      {ok === true ? 'Online' : ok === false ? 'Offline' : 'Optional'}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className={styles.quickLinks}>
            <h2 className={styles.sectionTitle}>Quick actions</h2>
            <div className={styles.quickGrid}>
              {[
                { to: '/admin/users', label: 'Manage users', icon: 'Users', color: '#0ea5e9' },
                { to: '/admin/doctors', label: 'View doctors', icon: 'Doctors', color: '#10b981' },
                { to: '/admin/appointments', label: 'Manage appointments', icon: 'Calendar', color: '#8b5cf6' },
                { to: '/admin/leaves', label: 'Review leave requests', icon: 'Leaves', color: '#f59e0b' },
              ].map(({ to, label, icon, color }) => (
                <a key={to} href={to} className={styles.quickCard} style={{ '--c': color }}>
                  <span className={styles.quickIcon}>{icon}</span>
                  <span className={styles.quickLabel}>{label}</span>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className={styles.quickArrow}><polyline points="9 18 15 12 9 6"/></svg>
                </a>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
