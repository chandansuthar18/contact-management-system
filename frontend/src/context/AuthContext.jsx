
import { createContext, useContext, useState, useCallback } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user,  setUser]  = useState(() => {
    try {
      const stored = localStorage.getItem('cms_user')
      return stored ? JSON.parse(stored) : null
    } catch {
      return null
    }
  })
  const [token, setToken] = useState(() => localStorage.getItem('cms_token') || null)

 
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

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside <AuthProvider>')
  }
  return context
}
