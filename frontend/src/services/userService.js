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

export function uploadAvatar(file) {
  const body = new FormData();
  body.append('file', file);
  return userApi('/api/v1/users/me/avatar', {
    method: 'POST',
    body,
  });
}

export function fetchCertifications() {
  return userApi('/api/v1/users/me/certifications');
}

export function createCertification(data) {
  return userApi('/api/v1/users/me/certifications', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function updateCertification(id, data) {
  return userApi(`/api/v1/users/me/certifications/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export function deleteCertification(id) {
  return userApi(`/api/v1/users/me/certifications/${id}`, {
    method: 'DELETE',
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
