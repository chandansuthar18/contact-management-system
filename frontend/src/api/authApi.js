
import api from './axiosInstance'

const authApi = {

  register: async (data) => {
    const response = await api.post('/auth/register', data)
    return response.data
  },
  
  login: async (data) => {
    const response = await api.post('/auth/login', data)
    return response.data
  },

  changePassword: async (data) => {
    const response = await api.put('/auth/change-password', data)
    return response.data
  },
}

export default authApi
