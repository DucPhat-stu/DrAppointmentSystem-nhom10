import { httpClient } from './httpClient.js';

export function createAppointment(payload) {
  return httpClient('/appointments', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function fetchAppointment(appointmentId) {
  return httpClient(`/appointments/${appointmentId}`);
}

