import { notificationApi } from './httpClient.js';

export function fetchNotifications({ page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });
  return notificationApi(`/api/v1/notifications?${params.toString()}`);
}

export function markNotificationRead(notificationId) {
  return notificationApi(`/api/v1/notifications/${notificationId}/read`, {
    method: 'PATCH',
  });
}

export function markNotificationsRead(ids) {
  return notificationApi('/api/v1/notifications/mark-read', {
    method: 'PATCH',
    body: JSON.stringify({ ids }),
  });
}
