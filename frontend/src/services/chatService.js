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
};
