// src/api/userApi.js
// User profile API calls
import api from './axiosInstance'

const userApi = {

  /**
   * Get the current user's profile.
   * GET /users/me
   * @returns UserDTO
   */
  getProfile: async () => {
    const response = await api.get('/users/me')
    return response.data
  },

  /**
   * Update the current user's profile.
   * PUT /users/me
   * @param {{ firstName, lastName, email, phone }} data
   * @returns UserDTO
   */
  updateProfile: async (data) => {
    const response = await api.put('/users/me', data)
    return response.data
  },
}

export default userApi
