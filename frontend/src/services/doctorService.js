import { httpClient } from './httpClient.js';

export function fetchDoctors() {
  return httpClient('/doctors');
}

export function fetchDoctorDetail(doctorId) {
  return httpClient(`/doctors/${doctorId}`);
}

