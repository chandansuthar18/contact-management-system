// src/context/AuthContext.jsx
// ─────────────────────────────────────────────────────────────────────────────
// AuthContext provides global authentication state to the entire React app.
//
// What it stores:
//   - user: the logged-in user object (or null)
//   - token: the JWT string (also in localStorage for persistence)
//   - login / logout helpers
//
// Usage anywhere in the app:
//   const { user, login, logout } = useAuth()
// ─────────────────────────────────────────────────────────────────────────────
import { createContext, useContext, useState, useCallback } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  // Initialize from localStorage so user stays logged in on page refresh
  const [user,  setUser]  = useState(() => {
    try {
      const stored = localStorage.getItem('cms_user')
      return stored ? JSON.parse(stored) : null
    } catch {
      return null
    }
  })
  const [token, setToken] = useState(() => localStorage.getItem('cms_token') || null)

  /**
   * Call after successful login or register.
   * Saves token and user to state AND localStorage.
   */
  const login = useCallback((authResponse) => {
    const { token: newToken, user: newUser } = authResponse
    setToken(newToken)
    setUser(newUser)
    localStorage.setItem('cms_token', newToken)
    localStorage.setItem('cms_user',  JSON.stringify(newUser))
  }, [])

  /**
   * Call when the user clicks "Logout".
   * Clears all stored auth data.
   */
  const logout = useCallback(() => {
    setToken(null)
    setUser(null)
    localStorage.removeItem('cms_token')
    localStorage.removeItem('cms_user')
  }, [])

  /**
   * Update stored user profile (after profile edit).
   */
  const updateUser = useCallback((updatedUser) => {
    setUser(updatedUser)
    localStorage.setItem('cms_user', JSON.stringify(updatedUser))
  }, [])

  const value = {
    user,
    token,
    isAuthenticated: !!token,
    login,
    logout,
    updateUser,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

/** Custom hook — use this instead of useContext(AuthContext) directly */
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside <AuthProvider>')
  }
  return context
}
