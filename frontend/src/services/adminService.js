import { authApi, appointmentApi, doctorApi } from './httpClient.js';

/* =========================================================
   Admin Service – calls to admin endpoints across services
   Uses existing httpClient instances (JWT auto-attached)
   ========================================================= */

// ---- Auth-service admin endpoints ----

export function fetchAdminUsers({ page = 0, size = 20, role = '', status = '' } = {}) {
  const params = new URLSearchParams({ page, size });
  if (role) params.set('role', role);
  if (status) params.set('status', status);
  return authApi(`/api/v1/admin/users?${params}`);
}

export function disableUser(userId) {
  return authApi(`/api/v1/admin/users/${userId}/disable`, { method: 'PUT' });
}

export function enableUser(userId) {
  return authApi(`/api/v1/admin/users/${userId}/enable`, { method: 'PUT' });
}

// ---- Doctor-service admin endpoints ----

export function fetchAdminDoctors({ page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({ page, size });
  return doctorApi(`/api/v1/admin/doctors?${params}`);
}

// ---- Appointment-service admin endpoints ----

export function fetchAdminAppointments({ page = 0, size = 20, status = '' } = {}) {
  const params = new URLSearchParams({ page, size });
  if (status) params.set('status', status);
  return appointmentApi(`/api/v1/admin/appointments?${params}`);
}

export function adminCancelAppointment(appointmentId, reason = '') {
  return appointmentApi(`/api/v1/admin/appointments/${appointmentId}/cancel`, {
    method: 'PUT',
    body: JSON.stringify({ reason }),
  });
}
