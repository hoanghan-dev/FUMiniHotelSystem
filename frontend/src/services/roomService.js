import api from './api';

export const roomService = {
  getRooms: async (params = {}) => {
    const response = await api.get('/api/rooms', { params });
    return response.data;
  },

  getRoomById: async (id) => {
    const response = await api.get(`/api/rooms/${id}`);
    return response.data;
  },

  createRoom: async (data) => {
    const response = await api.post('/api/rooms', data);
    return response.data;
  },

  updateRoom: async (id, data) => {
    const response = await api.put(`/api/rooms/${id}`, data);
    return response.data;
  },

  deleteRoom: async (id) => {
    const response = await api.delete(`/api/rooms/${id}`);
    return response.data;
  },
};
