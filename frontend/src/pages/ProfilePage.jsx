// src/pages/ProfilePage.jsx
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'react-toastify'
import { LogOut, Lock, User } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import authApi from '../api/authApi'
import Layout from '../components/Layout'
import Modal from '../components/Modal'

// ── Change Password Schema ───────────────────────────────────
const pwSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword:     z.string().min(6, 'New password must be at least 6 characters'),
  confirmPassword: z.string().min(1, 'Please confirm your new password'),
}).refine(d => d.newPassword === d.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword'],
})

export default function ProfilePage() {
  const { user, logout, updateUser } = useAuth()
  const navigate = useNavigate()
  const [showPwModal, setShowPwModal] = useState(false)
  const [pwLoading,   setPwLoading]   = useState(false)

  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    resolver: zodResolver(pwSchema),
    defaultValues: { currentPassword: '', newPassword: '', confirmPassword: '' },
  })

  // ── Handlers ─────────────────────────────────────────────
  const handleLogout = () => {
    logout()
    navigate('/login')
    toast.info('Signed out successfully')
  }

  const handleChangePassword = async (data) => {
    setPwLoading(true)
    try {
      await authApi.changePassword({
        currentPassword: data.currentPassword,
        newPassword:     data.newPassword,
      })
      toast.success('Password changed successfully!')
      setShowPwModal(false)
      reset()
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to change password'
      toast.error(msg)
    } finally {
      setPwLoading(false)
    }
  }

  const handleCloseModal = () => {
    setShowPwModal(false)
    reset()
  }

  // Avatar helpers
  const avatarBg   = '#60A5FA'
  const avatarText = `${user?.firstName?.[0] || ''}${user?.lastName?.[0] || ''}`.toUpperCase()

  return (
    <Layout>
      <h1 style={S.h1}>My Profile</h1>

      <div style={S.grid}>
        {/* ── Profile Card ── */}
        <div style={S.card}>
          {/* Avatar + Name */}
          <div style={S.avatarRow}>
            <div style={{ ...S.avatar, background: avatarBg }}>{avatarText}</div>
            <div>
              <div style={S.fullName}>{user?.firstName} {user?.lastName}</div>
              <div style={S.email}>{user?.email || user?.phone}</div>
            </div>
          </div>

          <hr style={S.divider} />

          {/* Profile Fields */}
          <div style={S.fields}>
            {[
              ['FIRST NAME',  user?.firstName  || '—'],
              ['LAST NAME',   user?.lastName   || '—'],
              ['EMAIL',       user?.email      || '—'],
              ['PHONE',       user?.phone      || '—'],
              ['MEMBER SINCE', user?.createdAt
                ? new Date(user.createdAt).toLocaleDateString('en-US', { year:'numeric', month:'long', day:'numeric' })
                : '—'],
            ].map(([label, value]) => (
              <div key={label} style={S.fieldRow}>
                <div style={S.fieldLabel}>{label}</div>
                <div style={S.fieldValue}>{value}</div>
              </div>
            ))}
          </div>
        </div>

        {/* ── Actions Panel ── */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {/* Security Card */}
          <div style={S.card}>
            <div style={S.actionCardIcon}><Lock size={20} color="#60A5FA" /></div>
            <div style={S.actionCardTitle}>Password & Security</div>
            <p style={S.actionCardDesc}>
              Keep your account secure by using a strong, unique password that you don&apos;t use elsewhere.
            </p>
            <button style={S.outlineBtn} onClick={() => setShowPwModal(true)}>
              <Lock size={14} /> Change Password
            </button>
          </div>

          {/* Account Card */}
          <div style={S.card}>
            <div style={S.actionCardIcon}><User size={20} color="#9CA3AF" /></div>
            <div style={S.actionCardTitle}>Account</div>
            <p style={S.actionCardDesc}>Sign out from all devices by logging out below.</p>
            <button style={S.dangerBtn} onClick={handleLogout}>
              <LogOut size={14} /> Sign Out
            </button>
          </div>
        </div>
      </div>

      {/* ── Modal 4: Change Password ── */}
      <Modal isOpen={showPwModal} onClose={handleCloseModal} title="Change Password" maxWidth={460}>
        <form onSubmit={handleSubmit(handleChangePassword)} style={{ display: 'flex', flexDirection: 'column', gap: 18 }} noValidate>

          <div style={S.field}>
            <label style={S.label}>CURRENT PASSWORD</label>
            <input {...register('currentPassword')} type="password" style={S.input} placeholder="Your current password" />
            {errors.currentPassword && <span style={S.error}>{errors.currentPassword.message}</span>}
          </div>

          <div style={S.field}>
            <label style={S.label}>NEW PASSWORD</label>
            <input {...register('newPassword')} type="password" style={S.input} placeholder="Min 6 characters" />
            {errors.newPassword && <span style={S.error}>{errors.newPassword.message}</span>}
          </div>

          <div style={S.field}>
            <label style={S.label}>CONFIRM NEW PASSWORD</label>
            <input {...register('confirmPassword')} type="password" style={S.input} placeholder="Repeat new password" />
            {errors.confirmPassword && <span style={S.error}>{errors.confirmPassword.message}</span>}
          </div>

          <hr style={S.divider} />

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10 }}>
            <button type="button" style={S.outlineBtn} onClick={handleCloseModal} disabled={pwLoading}>
              Cancel
            </button>
            <button type="submit" style={{ ...S.primaryBtn, opacity: pwLoading ? 0.7 : 1 }} disabled={pwLoading}>
              {pwLoading ? 'Updating…' : 'Update Password'}
            </button>
          </div>
        </form>
      </Modal>
    </Layout>
  )
}

const S = {
  h1:              { margin: '0 0 28px', fontSize: 26, fontWeight: 800, color: '#F0F2F5', letterSpacing: '-0.5px' },
  grid:            { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 20, alignItems: 'start' },
  card:            { background: '#181C27', border: '1px solid #252B3B', borderRadius: 16, padding: 28 },
  avatarRow:       { display: 'flex', alignItems: 'center', gap: 18, marginBottom: 24 },
  avatar:          { width: 68, height: 68, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, fontSize: 24, color: '#0F1117', flexShrink: 0 },
  fullName:        { fontSize: 20, fontWeight: 800, color: '#F0F2F5' },
  email:           { color: '#9CA3AF', fontSize: 14, marginTop: 3 },
  divider:         { border: 'none', borderTop: '1px solid #252B3B', margin: '20px 0' },
  fields:          { display: 'flex', flexDirection: 'column', gap: 16 },
  fieldRow:        { display: 'flex', flexDirection: 'column', gap: 4 },
  fieldLabel:      { fontSize: 11, fontWeight: 700, color: '#6B7280', letterSpacing: '0.5px', textTransform: 'uppercase' },
  fieldValue:      { fontSize: 15, color: '#F0F2F5' },
  actionCardIcon:  { marginBottom: 10 },
  actionCardTitle: { fontWeight: 700, fontSize: 16, color: '#F0F2F5', marginBottom: 8 },
  actionCardDesc:  { color: '#6B7280', fontSize: 13, marginBottom: 18, lineHeight: 1.6 },
  outlineBtn:      { display: 'flex', alignItems: 'center', gap: 6, background: 'transparent', border: '1px solid #252B3B', color: '#9CA3AF', borderRadius: 10, padding: '10px 18px', cursor: 'pointer', fontWeight: 600, fontSize: 14, width: '100%', justifyContent: 'center' },
  primaryBtn:      { background: '#60A5FA', color: '#0F1117', border: 'none', borderRadius: 10, padding: '10px 20px', fontWeight: 700, fontSize: 14, cursor: 'pointer' },
  dangerBtn:       { display: 'flex', alignItems: 'center', gap: 6, background: '#EF444415', border: '1px solid #EF444435', color: '#EF4444', borderRadius: 10, padding: '10px 18px', cursor: 'pointer', fontWeight: 700, fontSize: 14, width: '100%', justifyContent: 'center' },
  field:           { display: 'flex', flexDirection: 'column', gap: 6 },
  label:           { fontSize: 11, fontWeight: 700, color: '#9CA3AF', letterSpacing: '0.5px', textTransform: 'uppercase' },
  input:           { background: '#0F1117', border: '1px solid #252B3B', borderRadius: 10, padding: '11px 14px', color: '#F0F2F5', fontSize: 14, outline: 'none' },
  error:           { fontSize: 12, color: '#EF4444' },
}
