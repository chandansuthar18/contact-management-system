// src/components/ProtectedRoute.jsx
// Redirects unauthenticated users to /login.
// Wrap any route that requires a logged-in user.
//
// Usage in App.jsx:
//   <Route path="/contacts" element={<ProtectedRoute><ContactsPage /></ProtectedRoute>} />
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    // Redirect to login, saving the attempted URL so we can return after login
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return children
}
