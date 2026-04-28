import { appointmentApi } from './httpClient.js';

export function createAppointment(payload) {
  return appointmentApi('/api/v1/appointments', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function fetchAppointment(appointmentId) {
  return appointmentApi(`/api/v1/appointments/${appointmentId}`);
}

export function fetchAppointments({ status, page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });
  if (status) {
    params.set('status', status);
  }
  return appointmentApi(`/api/v1/appointments?${params.toString()}`);
}

export function cancelAppointment(appointmentId, reason, idempotencyKey = crypto.randomUUID()) {
  return appointmentApi(`/api/v1/appointments/${appointmentId}/cancel`, {
    method: 'PUT',
    headers: {
      'X-Idempotency-Key': idempotencyKey,
    },
    body: JSON.stringify({ reason }),
  });
}

export function rescheduleAppointment(appointmentId, payload, idempotencyKey = crypto.randomUUID()) {
  return appointmentApi(`/api/v1/appointments/${appointmentId}/reschedule`, {
    method: 'PUT',
    headers: {
      'X-Idempotency-Key': idempotencyKey,
    },
    body: JSON.stringify(payload),
  });
}
