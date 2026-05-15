
import { useForm, useFieldArray } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, X } from 'lucide-react'

// ── Zod Validation Schema ──────────────────────────────────────
const schema = z.object({
  firstName: z.string().min(1, 'First name is required').max(100),
  lastName:  z.string().max(100).optional(),
  title:     z.string().max(100).optional(),
  emails: z.array(z.object({
    label: z.string().min(1),
    email: z.string().email('Invalid email address'),
  })).optional(),
  phones: z.array(z.object({
    label: z.string().min(1),
    phone: z.string().regex(/^\+?[0-9]{7,15}$/, 'Invalid phone number'),
  })).optional(),
})

export default function ContactForm({ initialValues, onSubmit, onCancel, isLoading }) {
  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: initialValues || {
      firstName: '', lastName: '', title: '',
      emails: [{ label: 'work', email: '' }],
      phones: [{ label: 'work', phone: '' }],
    },
  })

  // useFieldArray lets us dynamically add/remove email/phone rows
  const emailFields = useFieldArray({ control, name: 'emails' })
  const phoneFields = useFieldArray({ control, name: 'phones' })

  return (
    <form onSubmit={handleSubmit(onSubmit)} style={styles.form}>

      {/* ── Basic Info ── */}
      <div style={styles.row}>
        <div style={styles.field}>
          <label style={styles.label}>FIRST NAME *</label>
          <input {...register('firstName')} style={styles.input} placeholder="First name" />
          {errors.firstName && <span style={styles.error}>{errors.firstName.message}</span>}
        </div>
        <div style={styles.field}>
          <label style={styles.label}>LAST NAME</label>
          <input {...register('lastName')} style={styles.input} placeholder="Last name" />
        </div>
      </div>

      <div style={styles.field}>
        <label style={styles.label}>TITLE / ROLE</label>
        <input {...register('title')} style={styles.input} placeholder="e.g. Software Engineer" />
      </div>

      {/* ── Email Addresses ── */}
      <div style={styles.section}>
        <div style={styles.sectionHeader}>
          <label style={styles.label}>EMAIL ADDRESSES</label>
          <button type="button" style={styles.addBtn}
            onClick={() => emailFields.append({ label: 'work', email: '' })}>
            <Plus size={14} /> Add Email
          </button>
        </div>
        {emailFields.fields.map((field, i) => (
          <div key={field.id} style={styles.multiRow}>
            <select {...register(`emails.${i}.label`)} style={{ ...styles.input, width: 110, flexShrink: 0 }}>
              <option value="work">Work</option>
              <option value="personal">Personal</option>
              <option value="other">Other</option>
            </select>
            <div style={{ flex: 1 }}>
              <input {...register(`emails.${i}.email`)} style={styles.input} placeholder="email@example.com" />
              {errors.emails?.[i]?.email && (
                <span style={styles.error}>{errors.emails[i].email.message}</span>
              )}
            </div>
            {emailFields.fields.length > 1 && (
              <button type="button" style={styles.removeBtn} onClick={() => emailFields.remove(i)}>
                <X size={14} />
              </button>
            )}
          </div>
        ))}
      </div>

      {/* ── Phone Numbers ── */}
      <div style={styles.section}>
        <div style={styles.sectionHeader}>
          <label style={styles.label}>PHONE NUMBERS</label>
          <button type="button" style={styles.addBtn}
            onClick={() => phoneFields.append({ label: 'work', phone: '' })}>
            <Plus size={14} /> Add Phone
          </button>
        </div>
        {phoneFields.fields.map((field, i) => (
          <div key={field.id} style={styles.multiRow}>
            <select {...register(`phones.${i}.label`)} style={{ ...styles.input, width: 110, flexShrink: 0 }}>
              <option value="work">Work</option>
              <option value="home">Home</option>
              <option value="personal">Personal</option>
              <option value="other">Other</option>
            </select>
            <div style={{ flex: 1 }}>
              <input {...register(`phones.${i}.phone`)} style={styles.input} placeholder="+1 555-0000" />
              {errors.phones?.[i]?.phone && (
                <span style={styles.error}>{errors.phones[i].phone.message}</span>
              )}
            </div>
            {phoneFields.fields.length > 1 && (
              <button type="button" style={styles.removeBtn} onClick={() => phoneFields.remove(i)}>
                <X size={14} />
              </button>
            )}
          </div>
        ))}
      </div>

      {/* ── Action Buttons ── */}
      <div style={styles.actions}>
        <button type="button" style={styles.cancelBtn} onClick={onCancel} disabled={isLoading}>
          Cancel
        </button>
        <button type="submit" style={styles.submitBtn} disabled={isLoading}>
          {isLoading ? 'Saving…' : 'Save Contact'}
        </button>
      </div>
    </form>
  )
}

const styles = {
  form:          { display: 'flex', flexDirection: 'column', gap: 16 },
  row:           { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 },
  field:         { display: 'flex', flexDirection: 'column', gap: 6 },
  label:         { fontSize: 11, fontWeight: 700, color: '#9CA3AF', letterSpacing: '0.5px', textTransform: 'uppercase' },
  input:         { background: '#0F1117', border: '1px solid #252B3B', borderRadius: 10, padding: '10px 13px', color: '#F0F2F5', fontSize: 14, outline: 'none', width: '100%', boxSizing: 'border-box' },
  error:         { fontSize: 12, color: '#EF4444', marginTop: 3 },
  section:       { display: 'flex', flexDirection: 'column', gap: 8 },
  sectionHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  multiRow:      { display: 'flex', gap: 8, alignItems: 'flex-start' },
  addBtn:        { display: 'flex', alignItems: 'center', gap: 4, background: 'none', border: 'none', color: '#60A5FA', cursor: 'pointer', fontSize: 13, fontWeight: 600 },
  removeBtn:     { background: 'none', border: 'none', color: '#EF4444', cursor: 'pointer', padding: '10px 6px', flexShrink: 0 },
  actions:       { display: 'flex', justifyContent: 'flex-end', gap: 10, paddingTop: 8, borderTop: '1px solid #252B3B' },
  cancelBtn:     { background: 'transparent', border: '1px solid #252B3B', color: '#9CA3AF', borderRadius: 10, padding: '10px 20px', cursor: 'pointer', fontWeight: 600, fontSize: 14 },
  submitBtn:     { background: '#60A5FA', color: '#0F1117', border: 'none', borderRadius: 10, padding: '10px 20px', cursor: 'pointer', fontWeight: 700, fontSize: 14 },
}
