import { aiApi } from './httpClient.js';

export const chatService = {
  async checkSymptoms(text) {
    const response = await aiApi('/api/v1/ai/check', {
      method: 'POST',
      body: JSON.stringify({ text }),
    });

    return response.data;
  },

  async checkStructuredSymptoms(payload) {
    const response = await aiApi('/api/v1/ai/check/structured', {
      method: 'POST',
      body: JSON.stringify(payload),
    });

    return response.data;
  },

  async previewStructuredPrompt(payload) {
    const response = await aiApi('/api/v1/ai/preview/structured', {
      method: 'POST',
      body: JSON.stringify(payload),
    });

    return response.data;
  },

  async fetchHistory() {
    const response = await aiApi('/api/v1/ai/conversations');
    return response.data ?? [];
  },

  async sendFeedback({ messageId, rating, comment = '' }) {
    return aiApi('/api/v1/ai/feedback', {
      method: 'POST',
      body: JSON.stringify({ messageId, rating, comment }),
    });
  },

  async recommendDoctors(symptoms) {
    const response = await aiApi('/api/v1/ai/doctor-recommendations', {
      method: 'POST',
      body: JSON.stringify({ symptoms }),
    });
    return response.data ?? [];
  },

  async analyzeImage(file) {
    const body = new FormData();
    body.append('file', file);
    const response = await aiApi('/api/v1/ai/image-analysis', {
      method: 'POST',
      body,
    });
    return response.data;
  },
};
