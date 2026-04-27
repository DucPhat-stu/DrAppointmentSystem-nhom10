import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchAvailableDoctors } from '../services/doctorService.js';
import styles from './DoctorListPage.module.css';

function today() {
  return new Date().toISOString().slice(0, 10);
}

function bookingUrl(doctor) {
  const params = new URLSearchParams({
    doctorId: doctor.doctorId,
    date: doctor.date,
    doctorName: doctor.fullName,
  });
  const specialty = doctor.specialty ?? doctor.department;
  if (specialty) {
    params.set('specialty', specialty);
  }
  return `/appointments/book?${params.toString()}`;
}

export default function DoctorListPage() {
  const [date, setDate] = useState(today());
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function loadDoctors(nextDate = date) {
    setLoading(true);
    setError('');
    try {
      const response = await fetchAvailableDoctors(nextDate);
      setDoctors(response.data ?? []);
    } catch (err) {
      setError(err.message ?? 'Unable to load available doctors');
      setDoctors([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDoctors();
  }, []);

  async function applyDate(event) {
    event.preventDefault();
    await loadDoctors(date);
  }

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Doctors</p>
          <h1 className={styles.title}>Available doctors</h1>
        </div>
        <button className={styles.secondaryButton} type="button" onClick={() => loadDoctors()} disabled={loading}>
          Refresh
        </button>
      </div>

      {error && <div className={styles.alert}>{error}</div>}

      <form className={styles.filters} onSubmit={applyDate}>
        <label>
          <span>Date</span>
          <input
            type="date"
            value={date}
            onChange={(event) => setDate(event.target.value)}
            disabled={loading}
            required
          />
        </label>
        <button className={styles.primaryButton} type="submit" disabled={loading}>
          Search
        </button>
      </form>

      <section className={styles.panel}>
        <div className={styles.panelHeader}>
          <h2>Results</h2>
          <span>{doctors.length} doctors</span>
        </div>

        {loading ? (
          <p className={styles.empty}>Loading doctors...</p>
        ) : doctors.length === 0 ? (
          <p className={styles.empty}>No doctors have available slots for this date.</p>
        ) : (
          <div className={styles.grid}>
            {doctors.map((doctor) => (
              <article className={styles.card} key={doctor.doctorId}>
                <div>
                  <strong>{doctor.fullName}</strong>
                  <small>{doctor.specialty ?? doctor.department ?? 'General care'}</small>
                  <span>{doctor.availableSlots} available slots</span>
                </div>
                <Link className={styles.primaryButton} to={bookingUrl(doctor)}>
                  Book
                </Link>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  );
}
