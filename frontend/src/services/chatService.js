import { aiApi } from './httpClient.js';

export const chatService = {
  async checkSymptoms(text) {
    const response = await aiApi('/api/v1/ai/check', {
      method: 'POST',
      body: JSON.stringify({ text }),
    });

    return response.data;
  },
};
