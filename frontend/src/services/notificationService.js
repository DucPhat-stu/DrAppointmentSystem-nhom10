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

export function fetchNotificationPreferences() {
  return notificationApi('/api/v1/notifications/preferences');
}

export function updateNotificationPreferences(data) {
  return notificationApi('/api/v1/notifications/preferences', {
    method: 'PATCH',
    body: JSON.stringify(data),
  });
}

export function broadcastNotification({ recipientIds, title, content, type = 'BROADCAST' }) {
  return notificationApi('/api/v1/admin/notifications/broadcast', {
    method: 'POST',
    body: JSON.stringify({ recipientIds, title, content, type }),
  });
}
