// src/api/contactsApi.js
// All contacts CRUD API calls
import api from './axiosInstance'

const contactsApi = {

  /**
   * Get paginated contacts (with optional search).
   * GET /contacts?page=0&size=10&search=john&sortBy=firstName
   * @returns {{ content, page, size, totalElements, totalPages, last }}
   */
  getContacts: async ({ page = 0, size = 10, search = '', sortBy = 'firstName' } = {}) => {
    const response = await api.get('/contacts', {
      params: { page, size, search, sortBy },
    })
    return response.data
  },

  /**
   * Get a single contact by ID.
   * GET /contacts/:id
   * @returns ContactDTO
   */
  getContact: async (id) => {
    const response = await api.get(`/contacts/${id}`)
    return response.data
  },

  /**
   * Create a new contact.
   * POST /contacts
   * @param {{ firstName, lastName, title, emails, phones }} data
   * @returns ContactDTO
   */
  createContact: async (data) => {
    const response = await api.post('/contacts', data)
    return response.data
  },

  /**
   * Update an existing contact.
   * PUT /contacts/:id
   * @param {number} id
   * @param {{ firstName, lastName, title, emails, phones }} data
   * @returns ContactDTO
   */
  updateContact: async (id, data) => {
    const response = await api.put(`/contacts/${id}`, data)
    return response.data
  },

  /**
   * Delete a contact.
   * DELETE /contacts/:id
   * @returns void (204 No Content)
   */
  deleteContact: async (id) => {
    await api.delete(`/contacts/${id}`)
  },
}

export default contactsApi
