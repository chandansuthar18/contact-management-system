// src/pages/RegisterPage.jsx
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'react-toastify'
import { useAuth } from '../context/AuthContext'
import authApi from '../api/authApi'

const schema = z.object({
  firstName: z.string().min(1, 'First name is required').max(100),
  lastName: z.string().max(100).optional(),
  email: z.string().email('Invalid email').optional().or(z.literal('')),
  phone: z.string().regex(/^\+?[0-9]{7,15}$/, 'Invalid phone').optional().or(z.literal('')),
  password: z.string().min(6, 'Password must be at least 6 characters').max(100),
  confirm: z.string().min(1, 'Please confirm your password'),
}).refine(d => d.email || d.phone, {
  message: 'Either email or phone number is required',
  path: ['email'],
}).refine(d => d.password === d.confirm, {
  message: 'Passwords do not match',
  path: ['confirm'],
})

export default function RegisterPage() {
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { firstName: '', lastName: '', email: '', phone: '', password: '', confirm: '' },
  })

  const onSubmit = async ({ confirm, ...data }) => {
    // strip empty optional fields before sending
    const payload = Object.fromEntries(
      Object.entries(data).filter(([, v]) => v !== '')
    )
    setLoading(true)
    try {
      const response = await authApi.register(payload)
      login(response)
      toast.success(`Account created! Welcome, ${response.user.firstName}!`)
      navigate('/contacts', { replace: true })
    } catch (err) {
      const msg = err.response?.data?.message || 'Registration failed. Please try again.'
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  const Field = ({ name, label, type = 'text', placeholder }) => (
    <div style={S.field}>
      <label style={S.label}>{label}</label>
      <input {...register(name)} type={type} style={S.input} placeholder={placeholder} />
      {errors[name] && <span style={S.error}>{errors[name].message}</span>}
    </div>
  )

  return (
    <div style={S.page}>
      <div style={S.card}>
        <div style={S.cardHeader}>
          <div style={S.icon}>✨</div>
          <h1 style={S.title}>Create New account</h1>
          <p style={S.subtitle}>Start managing your contacts today</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} style={S.form} noValidate>
          <div style={S.twoCol}>
            <Field name="firstName" label="FIRST NAME *" placeholder="Chandan" />
            <Field name="lastName" label="LAST NAME" placeholder="Kumar" />
          </div>
          <Field name="email" label="EMAIL" type="email" placeholder="chandan@gmail.com" />
          <Field name="phone" label="PHONE (optional)" placeholder="+923499999999" />
          <Field name="password" label="PASSWORD *" type="password" placeholder="Min 6 characters" />
          <Field name="confirm" label="CONFIRM PASSWORD *" type="password" placeholder="Repeat password" />

          <button type="submit" style={{ ...S.submitBtn, opacity: loading ? 0.7 : 1 }} disabled={loading}>
            {loading ? 'Creating account…' : 'Create Account'}
          </button>
        </form>

        <p style={S.footer}>
          Already registered?{' '}
          <Link to="/login" style={S.link}>Sign in</Link>
        </p>
      </div>
    </div>
  )
}

const S = {
  page: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#0F1117', padding: 24, fontFamily: "'DM Sans', sans-serif" },
  card: { background: '#181C27', border: '1px solid #252B3B', borderRadius: 20, padding: 40, width: '100%', maxWidth: 480 },
  cardHeader: { textAlign: 'center', marginBottom: 28 },
  icon: { fontSize: 42, marginBottom: 10 },
  title: { margin: '0 0 6px', fontSize: 24, fontWeight: 800, color: '#F0F2F5', letterSpacing: '-0.5px' },
  subtitle: { margin: 0, color: '#6B7280', fontSize: 14 },
  form: { display: 'flex', flexDirection: 'column', gap: 16 },
  twoCol: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 },
  field: { display: 'flex', flexDirection: 'column', gap: 6 },
  label: { fontSize: 11, fontWeight: 700, color: '#9CA3AF', letterSpacing: '0.5px', textTransform: 'uppercase' },
  input: { background: '#0F1117', border: '1px solid #252B3B', borderRadius: 10, padding: '11px 14px', color: '#F0F2F5', fontSize: 14, outline: 'none' },
  error: { fontSize: 12, color: '#EF4444' },
  submitBtn: { background: '#60A5FA', color: '#0F1117', border: 'none', borderRadius: 10, padding: '12px', fontWeight: 700, fontSize: 15, cursor: 'pointer', marginTop: 4 },
  footer: { textAlign: 'center', marginTop: 24, color: '#6B7280', fontSize: 14 },
  link: { color: '#60A5FA', fontWeight: 700, textDecoration: 'none' },
}
