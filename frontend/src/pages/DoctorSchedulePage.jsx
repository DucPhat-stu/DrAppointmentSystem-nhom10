import { useEffect, useMemo, useState } from 'react';
import {
  createDoctorSchedule,
  deleteDoctorSchedule,
  fetchDoctorSchedules,
  updateDoctorSchedule,
} from '../services/doctorService.js';
import styles from './DoctorSchedulePage.module.css';

function toInputDate(date) {
  return date.toISOString().slice(0, 10);
}

function startOfWeek(date) {
  const copy = new Date(date);
  const day = copy.getDay() || 7;
  copy.setDate(copy.getDate() - day + 1);
  copy.setHours(0, 0, 0, 0);
  return copy;
}

function formatDisplayDate(value) {
  return new Intl.DateTimeFormat('en', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
  }).format(new Date(`${value}T00:00:00`));
}

export default function DoctorSchedulePage() {
  const [schedules, setSchedules] = useState([]);
  const [selectedDate, setSelectedDate] = useState(toInputDate(new Date()));
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const weekDays = useMemo(() => {
    const weekStart = startOfWeek(new Date(`${selectedDate}T00:00:00`));
    return Array.from({ length: 7 }, (_, index) => {
      const day = new Date(weekStart);
      day.setDate(weekStart.getDate() + index);
      return toInputDate(day);
    });
  }, [selectedDate]);

  const scheduleDates = useMemo(
    () => new Set(schedules.map((schedule) => schedule.date)),
    [schedules],
  );

  async function loadSchedules() {
    setLoading(true);
    setError('');
    try {
      const response = await fetchDoctorSchedules();
      setSchedules(response.data ?? []);
    } catch (err) {
      setError(err.message ?? 'Unable to load schedules');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadSchedules();
  }, []);

  function startEdit(schedule) {
    setEditingId(schedule.id);
    setSelectedDate(schedule.date);
    setError('');
  }

  function resetForm() {
    setEditingId(null);
    setSelectedDate(toInputDate(new Date()));
    setError('');
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setError('');

    try {
      if (editingId) {
        await updateDoctorSchedule(editingId, { date: selectedDate });
      } else {
        await createDoctorSchedule({ date: selectedDate });
      }
      await loadSchedules();
      resetForm();
    } catch (err) {
      setError(err.message ?? 'Unable to save schedule');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(scheduleId) {
    setSaving(true);
    setError('');
    try {
      await deleteDoctorSchedule(scheduleId);
      await loadSchedules();
      if (editingId === scheduleId) {
        resetForm();
      }
    } catch (err) {
      setError(err.message ?? 'Unable to delete schedule');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className={styles.page}>
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Doctor Schedule</p>
          <h1 className={styles.title}>Working days</h1>
        </div>
        <button className={styles.secondaryButton} type="button" onClick={loadSchedules} disabled={loading || saving}>
          Refresh
        </button>
      </div>

      {error && <div className={styles.alert}>{error}</div>}

      <div className={styles.grid}>
        <form className={styles.form} onSubmit={handleSubmit}>
          <label className={styles.label} htmlFor="schedule-date">Work date</label>
          <input
            id="schedule-date"
            className={styles.input}
            type="date"
            value={selectedDate}
            onChange={(event) => setSelectedDate(event.target.value)}
            disabled={saving}
            required
          />
          <div className={styles.actions}>
            <button className={styles.primaryButton} type="submit" disabled={saving}>
              {editingId ? 'Update' : 'Create'}
            </button>
            {editingId && (
              <button className={styles.secondaryButton} type="button" onClick={resetForm} disabled={saving}>
                Cancel
              </button>
            )}
          </div>
        </form>

        <div className={styles.weekPanel}>
          <div className={styles.weekGrid}>
            {weekDays.map((day) => (
              <button
                key={day}
                type="button"
                className={`${styles.dayTile} ${scheduleDates.has(day) ? styles.hasSchedule : ''}`}
                onClick={() => setSelectedDate(day)}
              >
                <span>{formatDisplayDate(day)}</span>
                <strong>{scheduleDates.has(day) ? 'Working' : 'Open'}</strong>
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className={styles.list}>
        <div className={styles.listHeader}>
          <h2>Schedule list</h2>
          <span>{schedules.length} days</span>
        </div>

        {loading ? (
          <p className={styles.empty}>Loading schedules...</p>
        ) : schedules.length === 0 ? (
          <p className={styles.empty}>No working days yet.</p>
        ) : (
          schedules.map((schedule) => (
            <div className={styles.row} key={schedule.id}>
              <div>
                <strong>{formatDisplayDate(schedule.date)}</strong>
                <span>{schedule.date}</span>
              </div>
              <div className={styles.rowActions}>
                <button className={styles.secondaryButton} type="button" onClick={() => startEdit(schedule)} disabled={saving}>
                  Edit
                </button>
                <button className={styles.dangerButton} type="button" onClick={() => handleDelete(schedule.id)} disabled={saving}>
                  Delete
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </section>
  );
}
