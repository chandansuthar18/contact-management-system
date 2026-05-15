
import api from './axiosInstance'

const contactsApi = {

  getContacts: async ({ page = 0, size = 10, search = '', sortBy = 'firstName' } = {}) => {
    const response = await api.get('/contacts', {
      params: { page, size, search, sortBy },
    })
    return response.data
  },

  getContact: async (id) => {
    const response = await api.get(`/contacts/${id}`)
    return response.data
  },

  createContact: async (data) => {
    const response = await api.post('/contacts', data)
    return response.data
  },

  updateContact: async (id, data) => {
    const response = await api.put(`/contacts/${id}`, data)
    return response.data
  },

  deleteContact: async (id) => {
    await api.delete(`/contacts/${id}`)
  },
}

export default contactsApi
