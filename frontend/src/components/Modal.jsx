// src/components/Modal.jsx
// Reusable modal dialog component.
// Closes when clicking the backdrop or the X button.
import { useEffect } from 'react'
import { X } from 'lucide-react'

export default function Modal({ isOpen, onClose, title, children, maxWidth = 520 }) {
  // Close on Escape key press
  useEffect(() => {
    if (!isOpen) return
    const handleKey = (e) => { if (e.key === 'Escape') onClose() }
    window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  }, [isOpen, onClose])

  // Prevent page scrolling when modal is open
  useEffect(() => {
    document.body.style.overflow = isOpen ? 'hidden' : ''
    return () => { document.body.style.overflow = '' }
  }, [isOpen])

  if (!isOpen) return null

  return (
    <div
      style={styles.backdrop}
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div style={{ ...styles.box, maxWidth }}>
        {/* Header */}
        <div style={styles.header}>
          <h2 style={styles.title}>{title}</h2>
          <button style={styles.closeBtn} onClick={onClose} aria-label="Close modal">
            <X size={18} />
          </button>
        </div>

        {/* Content */}
        <div style={styles.body}>{children}</div>
      </div>
    </div>
  )
}

const styles = {
  backdrop: {
    position: 'fixed', inset: 0,
    background: 'rgba(0,0,0,0.6)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    zIndex: 1000, padding: 16,
    backdropFilter: 'blur(4px)',
  },
  box: {
    background: '#181C27', border: '1px solid #252B3B', borderRadius: 20,
    width: '100%', maxHeight: '90vh', overflowY: 'auto',
    animation: 'slideUp 0.2s ease',
  },
  header: {
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    padding: '20px 24px', borderBottom: '1px solid #252B3B',
  },
  title:    { margin: 0, fontSize: 18, fontWeight: 700, color: '#F0F2F5' },
  closeBtn: { background: 'none', border: 'none', color: '#9CA3AF', cursor: 'pointer', padding: 4, borderRadius: 6 },
  body:     { padding: 24 },
}
