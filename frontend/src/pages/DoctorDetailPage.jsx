import { useEffect, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { fetchAvailableSlots, fetchDoctorDetail } from '../services/doctorService.js';
import styles from './Phase3Pages.module.css';

function today() {
  return new Date().toISOString().slice(0, 10);
}

function formatTime(value) {
  if (!value) return '-';
  return new Intl.DateTimeFormat('en', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function buildBookingUrl(doctor, date, slot) {
  const params = new URLSearchParams({
    doctorId: doctor.userId,
    doctorName: doctor.fullName,
    date,
  });
  if (doctor.specialty || doctor.department) {
    params.set('specialty', doctor.specialty ?? doctor.department);
  }
  if (slot?.id) {
    params.set('slotId', slot.id);
  }
  return `/appointments/book?${params.toString()}`;
}

export default function DoctorDetailPage() {
  const { doctorId } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const [date, setDate] = useState(searchParams.get('date') ?? today());
  const [doctor, setDoctor] = useState(null);
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function load(nextDate = date) {
    setLoading(true);
    setError('');
    try {
      const [doctorResponse, slotResponse] = await Promise.all([
        fetchDoctorDetail(doctorId),
        fetchAvailableSlots(doctorId, nextDate),
      ]);
      setDoctor(doctorResponse.data);
      setSlots(slotResponse.data ?? []);
      setSearchParams({ date: nextDate });
    } catch (err) {
      setError(err.message ?? 'Unable to load doctor profile');
      setSlots([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load(date);
  }, [doctorId]);

  function applyDate(event) {
    event.preventDefault();
    load(date);
  }

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Doctor profile</p>
          <h1 className={styles.title}>{doctor?.fullName ?? 'Doctor details'}</h1>
          <p className={styles.subtitle}>{doctor?.specialty ?? doctor?.department ?? 'General care'}</p>
        </div>
        {doctor && (
          <Link className={styles.primaryButton} to={buildBookingUrl(doctor, date)}>
            Book appointment
          </Link>
        )}
      </div>

      {error && <div className={styles.alert}>{error}</div>}

      <div className={styles.layout}>
        <section className={styles.panel}>
          <div className={styles.panelHeader}>
            <h2>Available slots</h2>
            <span>{slots.length} open</span>
          </div>

          <form className={styles.filters} onSubmit={applyDate}>
            <label className={styles.field}>
              <span>Date</span>
              <input type="date" value={date} onChange={(event) => setDate(event.target.value)} disabled={loading} />
            </label>
            <button className={styles.primaryButton} type="submit" disabled={loading}>
              Search
            </button>
          </form>

          {loading ? (
            <p className={styles.empty}>Loading slots...</p>
          ) : slots.length === 0 ? (
            <p className={styles.empty}>No available slots for this date.</p>
          ) : (
            <div className={styles.slotGrid}>
              {slots.map((slot) => (
                <Link className={styles.slot} key={slot.id} to={buildBookingUrl(doctor, date, slot)}>
                  <strong>{formatTime(slot.startTime)} - {formatTime(slot.endTime)}</strong>
                  <span className={styles.meta}>{slot.status}</span>
                </Link>
              ))}
            </div>
          )}
        </section>

        <aside className={styles.panel}>
          <div className={styles.panelHeader}>
            <h2>Profile</h2>
          </div>
          <dl className={styles.detailGrid}>
            <div className={styles.detailBox}>
              <dt>Email</dt>
              <dd>{doctor?.email ?? '-'}</dd>
            </div>
            <div className={styles.detailBox}>
              <dt>Department</dt>
              <dd>{doctor?.department ?? '-'}</dd>
            </div>
            <div className={styles.detailBox}>
              <dt>Specialty</dt>
              <dd>{doctor?.specialty ?? '-'}</dd>
            </div>
            <div className={styles.detailBox}>
              <dt>Doctor ID</dt>
              <dd>{doctorId}</dd>
            </div>
          </dl>
        </aside>
      </div>
    </section>
  );
}
