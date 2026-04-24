import { userApi } from './httpClient.js';

/**
 * Fetch current user's profile.
 */
export function fetchProfile() {
  return userApi('/api/v1/users/me');
}

/**
 * Update current user's profile.
 */
export function updateProfile(data) {
  return userApi('/api/v1/users/me', {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

/**
 * Fetch current user's medical records.
 */
export function fetchMedicalRecords() {
  return userApi('/api/v1/medical-records');
}

/**
 * Fetch a single medical record detail.
 */
export function fetchMedicalRecordDetail(recordId) {
  return userApi(`/api/v1/medical-records/${recordId}`);
}
