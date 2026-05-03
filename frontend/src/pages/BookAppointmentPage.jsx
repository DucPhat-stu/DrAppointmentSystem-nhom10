import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { createAppointment } from '../services/appointmentService.js';
import { fetchAvailableDoctors, fetchAvailableSlots } from '../services/doctorService.js';
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
  const [doctorName, setDoctorName] = useState(searchParams.get('doctorName') ?? '');
  const [specialty, setSpecialty] = useState(searchParams.get('specialty') ?? '');
  const [date, setDate] = useState(searchParams.get('date') ?? today());
  const [doctors, setDoctors] = useState([]);
  const [slots, setSlots] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [reason, setReason] = useState('');
  const [createdAppointment, setCreatedAppointment] = useState(null);
  const [loading, setLoading] = useState(false);
  const [doctorsLoading, setDoctorsLoading] = useState(false);
  const [booking, setBooking] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const canLoad = useMemo(() => looksLikeUuid(doctorId) && !!date, [doctorId, date]);

  const selectedDoctor = useMemo(
    () => doctors.find((doctor) => doctor.doctorId === doctorId) ?? null,
    [doctors, doctorId],
  );

  const loadSlots = useCallback(async (nextDoctorId = doctorId, nextDate = date, doctor = selectedDoctor) => {
    if (!looksLikeUuid(nextDoctorId)) {
      setError('Select a doctor with registered available slots.');
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
      const nextParams = { doctorId: nextDoctorId, date: nextDate };
      if (doctor?.fullName || doctorName) nextParams.doctorName = doctor?.fullName ?? doctorName;
      if (doctor?.specialty || doctor?.department || specialty) {
        nextParams.specialty = doctor?.specialty ?? doctor?.department ?? specialty;
      }
      setSearchParams(nextParams);
    } catch (err) {
      setError(err.message ?? 'Unable to load available slots');
      setSlots([]);
    } finally {
      setLoading(false);
    }
  }, [doctorId, date, doctorName, selectedDoctor, setSearchParams, specialty]);

  const loadDoctorsAndSlots = useCallback(async (nextDate = date, preferredDoctorId = doctorId) => {
    setDoctorsLoading(true);
    setError('');
    setMessage('');
    setCreatedAppointment(null);
    try {
      const response = await fetchAvailableDoctors(nextDate);
      const availableDoctors = response.data ?? [];
      setDoctors(availableDoctors);

      const nextDoctor = availableDoctors.find((doctor) => doctor.doctorId === preferredDoctorId)
        ?? availableDoctors[0]
        ?? null;

      if (!nextDoctor) {
        setDoctorId('');
        setDoctorName('');
        setSpecialty('');
        setSlots([]);
        setSelectedSlot(null);
        setSearchParams({ date: nextDate });
        return;
      }

      setDoctorId(nextDoctor.doctorId);
      setDoctorName(nextDoctor.fullName ?? '');
      setSpecialty(nextDoctor.specialty ?? nextDoctor.department ?? '');
      await loadSlots(nextDoctor.doctorId, nextDate, nextDoctor);
    } catch (err) {
      setError(err.message ?? 'Unable to load doctors');
      setDoctors([]);
      setSlots([]);
      setSelectedSlot(null);
    } finally {
      setDoctorsLoading(false);
    }
  }, [date, doctorId, loadSlots, setSearchParams]);

  useEffect(() => {
    const initialDate = searchParams.get('date') ?? date;
    const initialDoctorId = searchParams.get('doctorId') ?? '';
    setDate(initialDate);
    setDoctorName(searchParams.get('doctorName') ?? '');
    setSpecialty(searchParams.get('specialty') ?? '');
    loadDoctorsAndSlots(initialDate, initialDoctorId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function applyFilters(event) {
    event.preventDefault();
    loadDoctorsAndSlots(date, doctorId);
  }

  function handleDoctorChange(event) {
    const nextDoctorId = event.target.value;
    const nextDoctor = doctors.find((doctor) => doctor.doctorId === nextDoctorId) ?? null;
    setDoctorId(nextDoctorId);
    setDoctorName(nextDoctor?.fullName ?? '');
    setSpecialty(nextDoctor?.specialty ?? nextDoctor?.department ?? '');
    loadSlots(nextDoctorId, date, nextDoctor);
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
        <button className={styles.secondaryButton} type="button" onClick={() => loadSlots()} disabled={!canLoad || loading || doctorsLoading}>
          Refresh
        </button>
      </div>

      {error && <div className={styles.alert}>{error}</div>}
      {message && <div className={styles.success}>{message}</div>}

      <form className={styles.filters} onSubmit={applyFilters}>
        <label>
          <span>Doctor</span>
            <select
              value={doctorId}
              onChange={handleDoctorChange}
              disabled={loading || doctorsLoading || doctors.length === 0}
              required
            >
              {doctors.length === 0 ? (
                <option value="">No registered slots</option>
              ) : (
                doctors.map((doctor) => (
                  <option key={doctor.doctorId} value={doctor.doctorId}>
                    {doctor.fullName} - {doctor.specialty ?? doctor.department ?? 'General care'} ({doctor.availableSlots} slots)
                  </option>
                ))
              )}
            </select>
        </label>
        <label>
          <span>Date</span>
          <input
            type="date"
            value={date}
            onChange={(event) => setDate(event.target.value)}
            disabled={loading || doctorsLoading}
            required
          />
        </label>
        <button className={styles.primaryButton} type="submit" disabled={loading || doctorsLoading}>
          Find slots
        </button>
      </form>

      <div className={styles.layout}>
        <section className={styles.panel}>
          <div className={styles.panelHeader}>
            <h2>Available slots</h2>
            <span>{slots.length} open</span>
          </div>

          {loading || doctorsLoading ? (
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
                <dt>Doctor</dt>
                <dd>
                  <strong>{doctorName || `Doctor ${doctorId.slice(0, 8)}`}</strong>
                  {specialty && <span>{specialty}</span>}
                </dd>
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
              <Link to={`/appointments/${createdAppointment.id}`}>View details</Link>
            </div>
          )}
        </form>
      </div>
    </section>
  );
}
