import { aiApi } from './httpClient.js';

export const aiTemplateService = {
  async list() {
    const response = await aiApi('/api/v1/ai/templates');
    return response.data ?? [];
  },

  async create(payload) {
    const response = await aiApi('/api/v1/ai/templates', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    return response.data;
  },

  async update(id, payload) {
    const response = await aiApi(`/api/v1/ai/templates/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
    return response.data;
  },

  async activate(id) {
    const response = await aiApi(`/api/v1/ai/templates/${id}/activate`, {
      method: 'PATCH',
    });
    return response.data;
  },

  async preview(payload) {
    const response = await aiApi('/api/v1/ai/templates/preview', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    return response.data;
  },
};
