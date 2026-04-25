import { doctorApi } from './httpClient.js';

export function fetchDoctors() {
  return doctorApi('/api/v1/doctors');
}

export function fetchDoctorDetail(doctorId) {
  return doctorApi(`/api/v1/doctors/${doctorId}`);
}

export function fetchDoctorSchedules() {
  return doctorApi('/api/v1/doctors/schedules');
}

export function createDoctorSchedule(payload) {
  return doctorApi('/api/v1/doctors/schedules', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function updateDoctorSchedule(scheduleId, payload) {
  return doctorApi(`/api/v1/doctors/schedules/${scheduleId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function deleteDoctorSchedule(scheduleId) {
  return doctorApi(`/api/v1/doctors/schedules/${scheduleId}`, {
    method: 'DELETE',
  });
}

export function fetchScheduleTimeSlots(scheduleId) {
  return doctorApi(`/api/v1/doctors/schedules/${scheduleId}/time-slots`);
}

export function createTimeSlot(payload) {
  return doctorApi('/api/v1/doctors/time-slots', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function updateTimeSlot(slotId, payload) {
  return doctorApi(`/api/v1/doctors/time-slots/${slotId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function deleteTimeSlot(slotId) {
  return doctorApi(`/api/v1/doctors/time-slots/${slotId}`, {
    method: 'DELETE',
  });
}

export function fetchDoctorAppointments({ date, status, page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });
  if (date) {
    params.set('date', date);
  }
  if (status) {
    params.set('status', status);
  }
  return doctorApi(`/api/v1/doctors/appointments?${params.toString()}`);
}

export function fetchDoctorAppointment(appointmentId) {
  return doctorApi(`/api/v1/doctors/appointments/${appointmentId}`);
}

export function confirmDoctorAppointment(appointmentId, idempotencyKey) {
  return doctorApi(`/api/v1/doctors/appointments/${appointmentId}/confirm`, {
    method: 'PUT',
    headers: {
      'X-Idempotency-Key': idempotencyKey,
    },
    body: JSON.stringify({ reason: null }),
  });
}

export function rejectDoctorAppointment(appointmentId, idempotencyKey, reason) {
  return doctorApi(`/api/v1/doctors/appointments/${appointmentId}/reject`, {
    method: 'PUT',
    headers: {
      'X-Idempotency-Key': idempotencyKey,
    },
    body: JSON.stringify({ reason }),
  });
}

export function cancelDoctorAppointment(appointmentId, idempotencyKey, reason) {
  return doctorApi(`/api/v1/doctors/appointments/${appointmentId}/cancel`, {
    method: 'PUT',
    headers: {
      'X-Idempotency-Key': idempotencyKey,
    },
    body: JSON.stringify({ reason }),
  });
}
