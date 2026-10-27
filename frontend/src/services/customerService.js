import api from './api';

export const customerService = {
  getCustomers: async (params = {}) => {
    const response = await api.get('/api/customers', { params });
    return response.data;
  },

  getCustomerById: async (id) => {
    const response = await api.get(`/api/customers/${id}`);
    return response.data;
  },

  createCustomer: async (data) => {
    const response = await api.post('/api/customers', data);
    return response.data;
  },

  updateCustomer: async (id, data) => {
    const response = await api.put(`/api/customers/${id}`, data);
    return response.data;
  },

  deleteCustomer: async (id) => {
    const response = await api.delete(`/api/customers/${id}`);
    return response.data;
  },

  getMyProfile: async () => {
    const response = await api.get('/api/customers/me');
    return response.data;
  },

  updateMyProfile: async (data) => {
    const response = await api.put('/api/customers/me', data);
    return response.data;
  },
};
