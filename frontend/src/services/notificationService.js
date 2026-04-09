import { httpClient } from './httpClient.js';

export function fetchNotifications() {
  return httpClient('/notifications');
}

