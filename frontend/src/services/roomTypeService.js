import api from './api';

export const roomTypeService = {
  getRoomTypes: async () => {
    const response = await api.get('/api/room-types');
    return response.data;
  },

  getRoomTypeById: async (id) => {
    const response = await api.get(`/api/room-types/${id}`);
    return response.data;
  },
};
