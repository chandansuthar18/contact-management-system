// src/api/authApi.js
// All authentication API calls (register, login, change password)
import api from './axiosInstance'

const authApi = {

  /**
   * Register a new user.
   * POST /auth/register
   * @param {{ firstName, lastName, email, phone, password }} data
   * @returns {{ token, tokenType, user }}
   */
  register: async (data) => {
    const response = await api.post('/auth/register', data)
    return response.data
  },

  /**
   * Log in an existing user.
   * POST /auth/login
   * @param {{ identifier, password }} data  — identifier = email or phone
   * @returns {{ token, tokenType, user }}
   */
  login: async (data) => {
    const response = await api.post('/auth/login', data)
    return response.data
  },

  /**
   * Change the logged-in user's password.
   * PUT /auth/change-password
   * @param {{ currentPassword, newPassword }} data
   * @returns {{ message }}
   */
  changePassword: async (data) => {
    const response = await api.put('/auth/change-password', data)
    return response.data
  },
}

export default authApi