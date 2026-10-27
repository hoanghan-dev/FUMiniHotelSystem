import api from './api';

export const authService = {
  login: async (email, password) => {
    const response = await api.post('/api/auth/login', { email, password });
    return response.data;
  },

  register: async (customerData) => {
    const response = await api.post('/api/auth/register', customerData);
    return response.data;
  },
};
