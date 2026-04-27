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
