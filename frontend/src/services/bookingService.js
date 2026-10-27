import api from './api';

export const bookingService = {
  getAllBookings: async (params = {}) => {
    const response = await api.get('/api/bookings', { params });
    return response.data;
  },

  getBookingById: async (id) => {
    const response = await api.get(`/api/bookings/${id}`);
    return response.data;
  },

  createStaffBooking: async (data) => {
    const response = await api.post('/api/bookings', data);
    return response.data;
  },

  updateBooking: async (id, data) => {
    const response = await api.put(`/api/bookings/${id}`, data);
    return response.data;
  },

  deleteBooking: async (id) => {
    const response = await api.delete(`/api/bookings/${id}`);
    return response.data;
  },

  createOnlineBooking: async (data) => {
    const response = await api.post('/api/bookings/online', data);
    return response.data;
  },

  getMyBookingHistory: async () => {
    const response = await api.get('/api/bookings/my-history');
    return response.data;
  },
};
