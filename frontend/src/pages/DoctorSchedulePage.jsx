import { useEffect, useMemo, useState } from 'react';
import {
  createDoctorSchedule,
  createTimeSlot,
  deleteDoctorSchedule,
  deleteTimeSlot,
  fetchDoctorSchedules,
  fetchScheduleTimeSlots,
  updateDoctorSchedule,
  updateTimeSlot,
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

function toDateTimeInput(date, hour) {
  return `${date}T${String(hour).padStart(2, '0')}:00`;
}

function toApiDateTime(value) {
  return new Date(value).toISOString();
}

function toDateTimeInputValue(value) {
  const date = new Date(value);
  const offsetMs = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offsetMs).toISOString().slice(0, 16);
}

function formatTimeRange(slot) {
  const formatter = new Intl.DateTimeFormat('en', {
    hour: '2-digit',
    minute: '2-digit',
  });
  return `${formatter.format(new Date(slot.startTime))} - ${formatter.format(new Date(slot.endTime))}`;
}

export default function DoctorSchedulePage() {
  const [schedules, setSchedules] = useState([]);
  const [timeSlots, setTimeSlots] = useState([]);
  const [selectedDate, setSelectedDate] = useState(toInputDate(new Date()));
  const [editingId, setEditingId] = useState(null);
  const [editingSlotId, setEditingSlotId] = useState(null);
  const [slotForm, setSlotForm] = useState({
    startTime: `${toInputDate(new Date())}T08:00`,
    endTime: `${toInputDate(new Date())}T09:00`,
  });
  const [loading, setLoading] = useState(true);
  const [slotsLoading, setSlotsLoading] = useState(false);
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

  const selectedSchedule = useMemo(
    () => schedules.find((schedule) => schedule.date === selectedDate) ?? null,
    [schedules, selectedDate],
  );

  async function loadTimeSlots(scheduleId) {
    if (!scheduleId) {
      setTimeSlots([]);
      return;
    }

    setSlotsLoading(true);
    setError('');
    try {
      const response = await fetchScheduleTimeSlots(scheduleId);
      setTimeSlots(response.data ?? []);
    } catch (err) {
      setError(err.message ?? 'Unable to load time slots');
    } finally {
      setSlotsLoading(false);
    }
  }

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

  useEffect(() => {
    setSlotForm({
      startTime: toDateTimeInput(selectedDate, 8),
      endTime: toDateTimeInput(selectedDate, 9),
    });
    setEditingSlotId(null);
  }, [selectedDate]);

  useEffect(() => {
    loadTimeSlots(selectedSchedule?.id);
  }, [selectedSchedule?.id]);

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

  function startSlotEdit(slot) {
    if (slot.status === 'BOOKED') {
      return;
    }
    setEditingSlotId(slot.id);
    setSlotForm({
      startTime: toDateTimeInputValue(slot.startTime),
      endTime: toDateTimeInputValue(slot.endTime),
    });
  }

  function resetSlotForm() {
    setEditingSlotId(null);
    setSlotForm({
      startTime: toDateTimeInput(selectedDate, 8),
      endTime: toDateTimeInput(selectedDate, 9),
    });
  }

  async function handleSlotSubmit(event) {
    event.preventDefault();
    if (!selectedSchedule) {
      setError('Create a working day before adding time slots');
      return;
    }

    setSaving(true);
    setError('');
    const payload = {
      scheduleId: selectedSchedule.id,
      startTime: toApiDateTime(slotForm.startTime),
      endTime: toApiDateTime(slotForm.endTime),
    };

    try {
      if (editingSlotId) {
        await updateTimeSlot(editingSlotId, payload);
      } else {
        await createTimeSlot(payload);
      }
      await loadTimeSlots(selectedSchedule.id);
      resetSlotForm();
    } catch (err) {
      setError(err.message ?? 'Unable to save time slot');
    } finally {
      setSaving(false);
    }
  }

  async function handleSlotDelete(slotId) {
    setSaving(true);
    setError('');
    try {
      await deleteTimeSlot(slotId);
      await loadTimeSlots(selectedSchedule?.id);
      if (editingSlotId === slotId) {
        resetSlotForm();
      }
    } catch (err) {
      setError(err.message ?? 'Unable to delete time slot');
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

      <div className={styles.slotPanel}>
        <div className={styles.listHeader}>
          <h2>Time slots</h2>
          <span>{selectedSchedule ? formatDisplayDate(selectedSchedule.date) : 'No schedule selected'}</span>
        </div>

        <form className={styles.slotForm} onSubmit={handleSlotSubmit}>
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="slot-start">Start</label>
            <input
              id="slot-start"
              className={styles.input}
              type="datetime-local"
              value={slotForm.startTime}
              onChange={(event) => setSlotForm((current) => ({ ...current, startTime: event.target.value }))}
              disabled={!selectedSchedule || saving}
              required
            />
          </div>
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="slot-end">End</label>
            <input
              id="slot-end"
              className={styles.input}
              type="datetime-local"
              value={slotForm.endTime}
              onChange={(event) => setSlotForm((current) => ({ ...current, endTime: event.target.value }))}
              disabled={!selectedSchedule || saving}
              required
            />
          </div>
          <div className={styles.actions}>
            <button className={styles.primaryButton} type="submit" disabled={!selectedSchedule || saving}>
              {editingSlotId ? 'Update slot' : 'Add slot'}
            </button>
            {editingSlotId && (
              <button className={styles.secondaryButton} type="button" onClick={resetSlotForm} disabled={saving}>
                Cancel
              </button>
            )}
          </div>
        </form>

        {slotsLoading ? (
          <p className={styles.empty}>Loading time slots...</p>
        ) : timeSlots.length === 0 ? (
          <p className={styles.empty}>No time slots for this day.</p>
        ) : (
          <div className={styles.slotList}>
            {timeSlots.map((slot) => {
              const booked = slot.status === 'BOOKED';
              return (
                <div className={styles.slotRow} key={slot.id}>
                  <div>
                    <strong>{formatTimeRange(slot)}</strong>
                    <span className={`${styles.status} ${booked ? styles.booked : ''}`}>{slot.status}</span>
                  </div>
                  <div className={styles.rowActions}>
                    <button className={styles.secondaryButton} type="button" onClick={() => startSlotEdit(slot)} disabled={saving || booked}>
                      Edit
                    </button>
                    <button className={styles.dangerButton} type="button" onClick={() => handleSlotDelete(slot.id)} disabled={saving || booked}>
                      Delete
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </section>
  );
}
