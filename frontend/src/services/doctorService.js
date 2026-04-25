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
