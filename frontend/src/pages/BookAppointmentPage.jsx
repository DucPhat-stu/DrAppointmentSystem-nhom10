import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { createAppointment } from '../services/appointmentService.js';
import { fetchAvailableSlots } from '../services/doctorService.js';
import styles from './BookAppointmentPage.module.css';

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

function looksLikeUuid(value) {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(value);
}

export default function BookAppointmentPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [doctorId, setDoctorId] = useState(searchParams.get('doctorId') ?? '');
  const [date, setDate] = useState(searchParams.get('date') ?? today());
  const [slots, setSlots] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [reason, setReason] = useState('');
  const [createdAppointment, setCreatedAppointment] = useState(null);
  const [loading, setLoading] = useState(false);
  const [booking, setBooking] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const canLoad = useMemo(() => looksLikeUuid(doctorId) && !!date, [doctorId, date]);

  async function loadSlots(nextDoctorId = doctorId, nextDate = date) {
    if (!looksLikeUuid(nextDoctorId)) {
      setError('Enter a valid doctor ID.');
      setSlots([]);
      setSelectedSlot(null);
      return;
    }

    setLoading(true);
    setError('');
    setMessage('');
    setCreatedAppointment(null);
    setSelectedSlot(null);
    try {
      const response = await fetchAvailableSlots(nextDoctorId, nextDate);
      setSlots(response.data ?? []);
      setSearchParams({ doctorId: nextDoctorId, date: nextDate });
    } catch (err) {
      setError(err.message ?? 'Unable to load available slots');
      setSlots([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const initialDoctorId = searchParams.get('doctorId');
    if (initialDoctorId && looksLikeUuid(initialDoctorId)) {
      loadSlots(initialDoctorId, searchParams.get('date') ?? date);
    }
  }, []);

  function applyFilters(event) {
    event.preventDefault();
    loadSlots();
  }

  async function submitAppointment(event) {
    event.preventDefault();
    if (!selectedSlot) return;

    setBooking(true);
    setError('');
    setMessage('');
    try {
      const response = await createAppointment({
        doctorId,
        slotId: selectedSlot.id,
        reason,
      });
      await loadSlots(doctorId, date);
      setCreatedAppointment(response.data);
      setMessage('Appointment request created.');
    } catch (err) {
      setError(err.message ?? 'Unable to create appointment');
    } finally {
      setBooking(false);
    }
  }

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Booking</p>
          <h1 className={styles.title}>Choose an available slot</h1>
        </div>
        <button className={styles.secondaryButton} type="button" onClick={() => loadSlots()} disabled={!canLoad || loading}>
          Refresh
        </button>
      </div>

      {error && <div className={styles.alert}>{error}</div>}
      {message && <div className={styles.success}>{message}</div>}

      <form className={styles.filters} onSubmit={applyFilters}>
        <label>
          <span>Doctor ID</span>
          <input
            value={doctorId}
            onChange={(event) => setDoctorId(event.target.value.trim())}
            placeholder="doctor uuid"
            disabled={loading}
            required
          />
        </label>
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
        <button className={styles.primaryButton} type="submit" disabled={!canLoad || loading}>
          Find slots
        </button>
      </form>

      <div className={styles.layout}>
        <section className={styles.panel}>
          <div className={styles.panelHeader}>
            <h2>Available slots</h2>
            <span>{slots.length} open</span>
          </div>

          {loading ? (
            <p className={styles.empty}>Loading available slots...</p>
          ) : slots.length === 0 ? (
            <p className={styles.empty}>No available slots for this date.</p>
          ) : (
            <div className={styles.slotGrid}>
              {slots.map((slot) => (
                <button
                  type="button"
                  key={slot.id}
                  className={`${styles.slotButton} ${selectedSlot?.id === slot.id ? styles.selectedSlot : ''}`}
                  onClick={() => setSelectedSlot(slot)}
                >
                  <strong>{formatTime(slot.startTime)} - {formatTime(slot.endTime)}</strong>
                  <span>{slot.status}</span>
                </button>
              ))}
            </div>
          )}
        </section>

        <form className={styles.panel} onSubmit={submitAppointment}>
          <div className={styles.panelHeader}>
            <h2>Selection</h2>
            <span>{selectedSlot ? 'Ready' : 'None'}</span>
          </div>

          {selectedSlot ? (
            <dl className={styles.summary}>
              <div>
                <dt>Doctor ID</dt>
                <dd>{doctorId}</dd>
              </div>
              <div>
                <dt>Date</dt>
                <dd>{date}</dd>
              </div>
              <div>
                <dt>Time</dt>
                <dd>{formatTime(selectedSlot.startTime)} - {formatTime(selectedSlot.endTime)}</dd>
              </div>
              <div>
                <dt>Slot ID</dt>
                <dd>{selectedSlot.id}</dd>
              </div>
            </dl>
          ) : (
            <p className={styles.empty}>Select a slot to prepare the appointment details.</p>
          )}
          <label className={styles.reasonField}>
            <span>Reason</span>
            <textarea
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              maxLength={500}
              rows={4}
              disabled={!selectedSlot || booking}
            />
          </label>
          <button className={styles.primaryButton} type="submit" disabled={!selectedSlot || booking}>
            Request appointment
          </button>
          {createdAppointment && (
            <div className={styles.createdBox}>
              <strong>Appointment {createdAppointment.status}</strong>
              <span>{createdAppointment.id}</span>
            </div>
          )}
        </form>
      </div>
    </section>
  );
}
