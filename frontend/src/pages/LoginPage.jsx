// src/pages/LoginPage.jsx
import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'react-toastify'
import { useAuth } from '../context/AuthContext'
import authApi from '../api/authApi'

const schema = z.object({
  identifier: z.string().min(1, 'Email or phone is required'),
  password:   z.string().min(1, 'Password is required'),
})

export default function LoginPage() {
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate  = useNavigate()
  const location  = useLocation()
  const from      = location.state?.from?.pathname || '/contacts'

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data) => {
    setLoading(true)
    try {
      const response = await authApi.login(data)
      login(response)                        // save token + user in context
      toast.success(`Welcome back, ${response.user.firstName}!`)
      navigate(from, { replace: true })      // go to intended page or /contacts
    } catch (err) {
      const msg = err.response?.data?.message || 'Invalid credentials. Please try again.'
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={S.page}>
      <div style={S.card}>
        <div style={S.cardHeader}>
          <div style={S.icon}>📇</div>
          <h1 style={S.title}>Welcome back</h1>
          <p style={S.subtitle}>Sign in to your ContactBook account</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} style={S.form} noValidate>
          <div style={S.field}>
            <label style={S.label}>EMAIL OR PHONE</label>
            <input
              {...register('identifier')}
              style={S.input}
              placeholder="you@example.com or +1555..."
              autoFocus
            />
            {errors.identifier && <span style={S.error}>{errors.identifier.message}</span>}
          </div>

          <div style={S.field}>
            <label style={S.label}>PASSWORD</label>
            <input
              {...register('password')}
              type="password"
              style={S.input}
              placeholder="••••••••"
            />
            {errors.password && <span style={S.error}>{errors.password.message}</span>}
          </div>

          <button type="submit" style={{ ...S.submitBtn, opacity: loading ? 0.7 : 1 }} disabled={loading}>
            {loading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <p style={S.footer}>
          Don&apos;t have an account?{' '}
          <Link to="/register" style={S.link}>Create one</Link>
        </p>
      </div>
    </div>
  )
}

const S = {
  page:      { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#0F1117', padding: 24, fontFamily: "'DM Sans', sans-serif" },
  card:      { background: '#181C27', border: '1px solid #252B3B', borderRadius: 20, padding: 40, width: '100%', maxWidth: 420 },
  cardHeader:{ textAlign: 'center', marginBottom: 32 },
  icon:      { fontSize: 48, marginBottom: 12 },
  title:     { margin: '0 0 6px', fontSize: 26, fontWeight: 800, color: '#F0F2F5', letterSpacing: '-0.5px' },
  subtitle:  { margin: 0, color: '#6B7280', fontSize: 14 },
  form:      { display: 'flex', flexDirection: 'column', gap: 18 },
  field:     { display: 'flex', flexDirection: 'column', gap: 6 },
  label:     { fontSize: 11, fontWeight: 700, color: '#9CA3AF', letterSpacing: '0.5px', textTransform: 'uppercase' },
  input:     { background: '#0F1117', border: '1px solid #252B3B', borderRadius: 10, padding: '11px 14px', color: '#F0F2F5', fontSize: 14, outline: 'none', transition: 'border-color .2s' },
  error:     { fontSize: 12, color: '#EF4444' },
  submitBtn: { background: '#60A5FA', color: '#0F1117', border: 'none', borderRadius: 10, padding: '12px', fontWeight: 700, fontSize: 15, cursor: 'pointer', marginTop: 4 },
  footer:    { textAlign: 'center', marginTop: 24, color: '#6B7280', fontSize: 14 },
  link:      { color: '#60A5FA', fontWeight: 700, textDecoration: 'none' },
}
