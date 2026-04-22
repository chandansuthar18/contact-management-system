// src/components/Layout.jsx
// Main application layout: top navigation bar + page content.
// Used on all authenticated pages.
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Users, User, LogOut } from 'lucide-react'

export default function Layout({ children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div style={styles.root}>
      {/* ── Top Navigation Bar ── */}
      <header style={styles.navbar}>
        <div style={styles.navInner}>
          {/* Logo */}
          <div style={styles.logo}>📇 ContactBook</div>

          {/* Navigation Links */}
          <nav style={styles.navLinks}>
            <NavLink to="/contacts" style={({ isActive }) => ({
              ...styles.navLink,
              ...(isActive ? styles.navLinkActive : {}),
            })}>
              <Users size={16} /> Contacts
            </NavLink>

            <NavLink to="/profile" style={({ isActive }) => ({
              ...styles.navLink,
              ...(isActive ? styles.navLinkActive : {}),
            })}>
              <User size={16} /> Profile
            </NavLink>
          </nav>

          {/* User Info + Logout */}
          <div style={styles.userArea}>
            <span style={styles.userName}>
              {user?.firstName} {user?.lastName}
            </span>
            <button style={styles.logoutBtn} onClick={handleLogout} title="Logout">
              <LogOut size={16} /> Sign Out
            </button>
          </div>
        </div>
      </header>

      {/* ── Page Content ── */}
      <main style={styles.main}>
        {children}
      </main>
    </div>
  )
}

const styles = {
  root:        { minHeight: '100vh', background: '#0F1117', color: '#F0F2F5', fontFamily: "'DM Sans', sans-serif" },
  navbar:      { background: '#181C27', borderBottom: '1px solid #252B3B', position: 'sticky', top: 0, zIndex: 100 },
  navInner:    { maxWidth: 1100, margin: '0 auto', padding: '0 24px', height: 64, display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 },
  logo:        { fontWeight: 800, fontSize: 18, color: '#60A5FA', whiteSpace: 'nowrap' },
  navLinks:    { display: 'flex', gap: 4 },
  navLink:     { display: 'flex', alignItems: 'center', gap: 6, padding: '8px 14px', borderRadius: 8, color: '#9CA3AF', textDecoration: 'none', fontWeight: 600, fontSize: 14, transition: 'all .2s' },
  navLinkActive:{ background: '#60A5FA22', color: '#60A5FA' },
  userArea:    { display: 'flex', alignItems: 'center', gap: 12 },
  userName:    { fontSize: 14, color: '#9CA3AF', fontWeight: 600 },
  logoutBtn:   { display: 'flex', alignItems: 'center', gap: 6, background: 'transparent', border: '1px solid #252B3B', color: '#9CA3AF', borderRadius: 8, padding: '7px 12px', cursor: 'pointer', fontSize: 13, fontWeight: 600 },
  main:        { maxWidth: 1100, margin: '0 auto', padding: '32px 24px' },
}
