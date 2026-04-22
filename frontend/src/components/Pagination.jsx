// src/components/Pagination.jsx
// Reusable pagination bar.
export default function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null

  return (
    <div style={styles.root}>
      <button
        style={{ ...styles.btn, opacity: page === 0 ? 0.4 : 1 }}
        onClick={() => onPageChange(page - 1)}
        disabled={page === 0}
      >
        ← Prev
      </button>

      {Array.from({ length: totalPages }, (_, i) => (
        <button
          key={i}
          style={{ ...styles.btn, ...(i === page ? styles.active : {}) }}
          onClick={() => onPageChange(i)}
        >
          {i + 1}
        </button>
      ))}

      <button
        style={{ ...styles.btn, opacity: page >= totalPages - 1 ? 0.4 : 1 }}
        onClick={() => onPageChange(page + 1)}
        disabled={page >= totalPages - 1}
      >
        Next →
      </button>
    </div>
  )
}

const styles = {
  root:   { display: 'flex', gap: 8, justifyContent: 'center', marginTop: 24 },
  btn:    { background: 'transparent', border: '1px solid #252B3B', color: '#9CA3AF', borderRadius: 10, padding: '8px 16px', cursor: 'pointer', fontWeight: 600, fontSize: 14 },
  active: { background: '#60A5FA', color: '#0F1117', border: '1px solid #60A5FA' },
}
