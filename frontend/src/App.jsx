
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ToastContainer } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'

import { AuthProvider } from './context/AuthContext'
import ProtectedRoute  from './components/ProtectedRoute'
import LoginPage       from './pages/LoginPage'
import RegisterPage    from './pages/RegisterPage'
import ContactsPage    from './pages/ContactsPage'
import ProfilePage     from './pages/ProfilePage'

// React Query client — caches API responses, handles loading/error states
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,              // retry once on failure
      staleTime: 30_000,     // cache stays fresh for 30 seconds
    },
  },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          {/* Global font import */}
          <style>{`
            @import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;600;700;800&display=swap');
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body { background: #0F1117; color: #F0F2F5; font-family: 'DM Sans', sans-serif; }
            input:focus, select:focus, textarea:focus { border-color: #60A5FA !important; }
            ::-webkit-scrollbar { width: 6px; }
            ::-webkit-scrollbar-track { background: #0F1117; }
            ::-webkit-scrollbar-thumb { background: #252B3B; border-radius: 3px; }
          `}</style>

          <Routes>
            {/* Public routes */}
            <Route path="/login"    element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Protected routes — redirect to /login if not authenticated */}
            <Route path="/contacts" element={
              <ProtectedRoute><ContactsPage /></ProtectedRoute>
            }/>
            <Route path="/profile"  element={
              <ProtectedRoute><ProfilePage /></ProtectedRoute>
            }/>

            {/* Default redirect */}
            <Route path="/"   element={<Navigate to="/contacts" replace />} />
            <Route path="*"   element={<Navigate to="/contacts" replace />} />
          </Routes>
        </BrowserRouter>

        {/* Toast notifications (success, error, info) */}
        <ToastContainer
          position="top-right"
          autoClose={3000}
          theme="dark"
          toastStyle={{ background: '#181C27', border: '1px solid #252B3B', color: '#F0F2F5' }}
        />
      </AuthProvider>
    </QueryClientProvider>
  )
}
