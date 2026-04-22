// src/api/axiosInstance.js
// ─────────────────────────────────────────────────────────────────────────────
// Axios instance pre-configured for the CMS API.
//
// Features:
//   - Base URL: /api/v1  (proxied to http://localhost:8080 via vite.config.js)
//   - Request interceptor: automatically attaches JWT token to every request
//   - Response interceptor: redirects to /login on 401 (token expired/invalid)
// ─────────────────────────────────────────────────────────────────────────────
import axios from 'axios'

const axiosInstance = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,  // 10 second timeout
})

// ── REQUEST INTERCEPTOR ──────────────────────────────────────
// Runs before every outgoing request.
// Reads the JWT from localStorage and adds it to Authorization header.
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('cms_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ── RESPONSE INTERCEPTOR ─────────────────────────────────────
// Runs after every response (success or error).
// On 401: clear storage and redirect to login page.
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('cms_token')
      localStorage.removeItem('cms_user')
      window.location.href = '/login'
    }
    // Forward the error so individual API calls can handle it
    return Promise.reject(error)
  }
)

export default axiosInstance
