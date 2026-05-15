
import api from './axiosInstance'

const userApi = {
  getProfile: async () => {
    const response = await api.get('/users/me')
    return response.data
  },

  updateProfile: async (data) => {
    const response = await api.put('/users/me', data)
    return response.data
  },
}

export default userApi
