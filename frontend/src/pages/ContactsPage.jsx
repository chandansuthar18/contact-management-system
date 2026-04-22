// src/pages/ContactsPage.jsx
import { useState, useCallback } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'react-toastify'
import { Search, Plus, Eye, Pencil, Trash2, Mail, Phone } from 'lucide-react'
import contactsApi from '../api/contactsApi'
import Layout from '../components/Layout'
import Modal from '../components/Modal'
import ContactForm from '../components/ContactForm'
import Pagination from '../components/Pagination'

// Colour palette for avatars
const AVATAR_COLORS = ['#E63946','#2A9D8F','#E9C46A','#264653','#F4A261','#A8DADC','#457B9D','#6A4C93']
function avatarColor(name = '') {
  let h = 0
  for (const c of name) h = (h * 31 + c.charCodeAt(0)) % AVATAR_COLORS.length
  return AVATAR_COLORS[h]
}
function initials(first = '', last = '') {
  return `${first[0] || ''}${last[0] || ''}`.toUpperCase()
}

export default function ContactsPage() {
  const queryClient = useQueryClient()

  // ── List state ───────────────────────────────────────────
  const [search,  setSearch]  = useState('')
  const [page,    setPage]    = useState(0)
  const PAGE_SIZE = 8

  // ── Modal state ──────────────────────────────────────────
  const [viewContact,   setViewContact]   = useState(null)
  const [editContact,   setEditContact]   = useState(null)
  const [deleteContact, setDeleteContact] = useState(null)
  const [showCreate,    setShowCreate]    = useState(false)

  // ── React Query: fetch contacts ──────────────────────────
  const { data, isLoading, isError } = useQuery({
    queryKey: ['contacts', page, search],
    queryFn:  () => contactsApi.getContacts({ page, size: PAGE_SIZE, search }),
    keepPreviousData: true,   // keeps old data visible while new page loads
  })

  // ── Mutations ────────────────────────────────────────────
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['contacts'] })

  const createMutation = useMutation({
    mutationFn: contactsApi.createContact,
    onSuccess:  () => { toast.success('Contact created!'); setShowCreate(false); invalidate() },
    onError:    (e) => toast.error(e.response?.data?.message || 'Failed to create contact'),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => contactsApi.updateContact(id, data),
    onSuccess:  () => { toast.success('Contact updated!'); setEditContact(null); invalidate() },
    onError:    (e) => toast.error(e.response?.data?.message || 'Failed to update contact'),
  })

  const deleteMutation = useMutation({
    mutationFn: contactsApi.deleteContact,
    onSuccess:  () => { toast.success('Contact deleted'); setDeleteContact(null); invalidate() },
    onError:    (e) => toast.error(e.response?.data?.message || 'Failed to delete contact'),
  })

  // ── Search handler (reset page on new search) ────────────
  const handleSearch = useCallback((e) => {
    setSearch(e.target.value)
    setPage(0)
  }, [])

  return (
    <Layout>
      {/* ── Page Header ── */}
      <div style={S.header}>
        <div>
          <h1 style={S.h1}>Contacts</h1>
          <p style={S.sub}>
            {data ? `${data.totalElements} total` : '…'}
          </p>
        </div>
        <button style={S.primaryBtn} onClick={() => setShowCreate(true)}>
          <Plus size={16} /> New Contact
        </button>
      </div>

      {/* ── Search Bar ── */}
      <div style={S.searchWrap}>
        <Search size={16} style={S.searchIcon} />
        <input
          style={S.searchInput}
          placeholder="Search by first or last name…"
          value={search}
          onChange={handleSearch}
        />
      </div>

      {/* ── Contact List ── */}
      {isLoading && <p style={S.status}>Loading contacts…</p>}
      {isError   && <p style={{ ...S.status, color: '#EF4444' }}>Failed to load contacts.</p>}

      {data && data.content.length === 0 && (
        <div style={S.emptyState}>
          <div style={{ fontSize: 52, marginBottom: 12 }}>📭</div>
          <p style={{ color: '#9CA3AF' }}>
            {search ? `No contacts match "${search}"` : 'No contacts yet. Add your first one!'}
          </p>
        </div>
      )}

      <div style={S.list}>
        {data?.content.map(contact => (
          <ContactCard
            key={contact.id}
            contact={contact}
            onView={setViewContact}
            onEdit={setEditContact}
            onDelete={setDeleteContact}
          />
        ))}
      </div>

      {/* ── Pagination ── */}
      {data && (
        <Pagination
          page={data.page}
          totalPages={data.totalPages}
          onPageChange={setPage}
        />
      )}

      {/* ── Modal 1: View Contact ── */}
      <Modal isOpen={!!viewContact} onClose={() => setViewContact(null)} title="Contact Details">
        {viewContact && <ViewContactContent contact={viewContact} onEdit={() => { setEditContact(viewContact); setViewContact(null) }} onClose={() => setViewContact(null)} />}
      </Modal>

      {/* ── Modal 2: Create Contact ── */}
      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="New Contact">
        <ContactForm
          onSubmit={(data) => createMutation.mutate(data)}
          onCancel={() => setShowCreate(false)}
          isLoading={createMutation.isPending}
        />
      </Modal>

      {/* ── Modal 3: Edit Contact ── */}
      <Modal isOpen={!!editContact} onClose={() => setEditContact(null)} title="Edit Contact">
        {editContact && (
          <ContactForm
            initialValues={editContact}
            onSubmit={(data) => updateMutation.mutate({ id: editContact.id, data })}
            onCancel={() => setEditContact(null)}
            isLoading={updateMutation.isPending}
          />
        )}
      </Modal>

      {/* ── Modal 4: Delete Confirmation ── */}
      <Modal isOpen={!!deleteContact} onClose={() => setDeleteContact(null)} title="Delete Contact" maxWidth={440}>
        {deleteContact && (
          <div style={{ textAlign: 'center', padding: '8px 0' }}>
            <div style={{ fontSize: 52, marginBottom: 16 }}>⚠️</div>
            <p style={{ fontSize: 16, fontWeight: 600, color: '#F0F2F5', marginBottom: 8 }}>
              Delete {deleteContact.firstName} {deleteContact.lastName}?
            </p>
            <p style={{ color: '#6B7280', fontSize: 14, marginBottom: 28 }}>
              This will permanently remove the contact and all their details. This cannot be undone.
            </p>
            <div style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
              <button style={S.ghostBtn} onClick={() => setDeleteContact(null)}>Cancel</button>
              <button
                style={{ ...S.dangerBtn, opacity: deleteMutation.isPending ? 0.7 : 1 }}
                onClick={() => deleteMutation.mutate(deleteContact.id)}
                disabled={deleteMutation.isPending}
              >
                {deleteMutation.isPending ? 'Deleting…' : 'Delete Contact'}
              </button>
            </div>
          </div>
        )}
      </Modal>
    </Layout>
  )
}

// ── ContactCard ──────────────────────────────────────────────
function ContactCard({ contact, onView, onEdit, onDelete }) {
  const name = `${contact.firstName} ${contact.lastName || ''}`
  const bg   = avatarColor(name)

  return (
    <div style={S.card}>
      {/* Avatar */}
      <div style={{ ...S.avatar, background: bg }}>
        {initials(contact.firstName, contact.lastName)}
      </div>

      {/* Info */}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={S.contactName}>{contact.firstName} {contact.lastName}</div>
        {contact.title && <div style={S.contactTitle}>{contact.title}</div>}
        <div style={S.tags}>
          {contact.emails?.[0] && (
            <span style={S.tag}><Mail size={11} style={{ color: '#60A5FA' }} />{contact.emails[0].email}</span>
          )}
          {contact.phones?.[0] && (
            <span style={S.tag}><Phone size={11} style={{ color: '#34D399' }} />{contact.phones[0].phone}</span>
          )}
        </div>
      </div>

      {/* Action Buttons */}
      <div style={S.actions}>
        <button style={S.iconBtn} title="View"   onClick={() => onView(contact)}>  <Eye    size={16} /> </button>
        <button style={{ ...S.iconBtn, color: '#FBBF24' }} title="Edit" onClick={() => onEdit(contact)}><Pencil size={16} /></button>
        <button style={{ ...S.iconBtn, color: '#EF4444' }} title="Delete" onClick={() => onDelete(contact)}><Trash2 size={16} /></button>
      </div>
    </div>
  )
}

// ── ViewContactContent ───────────────────────────────────────
function ViewContactContent({ contact, onEdit, onClose }) {
  const name = `${contact.firstName} ${contact.lastName || ''}`
  const bg   = avatarColor(name)

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24 }}>
        <div style={{ ...S.avatar, width: 64, height: 64, fontSize: 22, borderRadius: '50%', background: bg, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 800 }}>
          {initials(contact.firstName, contact.lastName)}
        </div>
        <div>
          <div style={{ fontSize: 20, fontWeight: 800, color: '#F0F2F5' }}>{name}</div>
          {contact.title && <div style={{ color: '#9CA3AF', marginTop: 3 }}>{contact.title}</div>}
        </div>
      </div>

      <hr style={{ border: 'none', borderTop: '1px solid #252B3B', margin: '0 0 20px' }} />

      {contact.emails?.length > 0 && (
        <div style={{ marginBottom: 20 }}>
          <div style={S.detailLabel}>EMAIL ADDRESSES</div>
          {contact.emails.map((e, i) => (
            <div key={i} style={{ display: 'flex', gap: 10, alignItems: 'center', marginBottom: 8 }}>
              <span style={S.badge}>{e.label}</span>
              <span style={{ fontSize: 14 }}>{e.email}</span>
            </div>
          ))}
        </div>
      )}

      {contact.phones?.length > 0 && (
        <div style={{ marginBottom: 24 }}>
          <div style={S.detailLabel}>PHONE NUMBERS</div>
          {contact.phones.map((p, i) => (
            <div key={i} style={{ display: 'flex', gap: 10, alignItems: 'center', marginBottom: 8 }}>
              <span style={{ ...S.badge, background: '#34D39920', color: '#34D399', borderColor: '#34D39940' }}>{p.label}</span>
              <span style={{ fontSize: 14 }}>{p.phone}</span>
            </div>
          ))}
        </div>
      )}

      <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
        <button style={S.ghostBtn} onClick={onClose}>Close</button>
        <button style={S.primaryBtn} onClick={onEdit}><Pencil size={14} /> Edit</button>
      </div>
    </div>
  )
}

// ── Styles ────────────────────────────────────────────────────
const S = {
  header:      { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 28, flexWrap: 'wrap', gap: 12 },
  h1:          { margin: 0, fontSize: 26, fontWeight: 800, color: '#F0F2F5', letterSpacing: '-0.5px' },
  sub:         { margin: '4px 0 0', color: '#6B7280', fontSize: 14 },
  searchWrap:  { position: 'relative', marginBottom: 20 },
  searchIcon:  { position: 'absolute', left: 13, top: '50%', transform: 'translateY(-50%)', color: '#6B7280' },
  searchInput: { width: '100%', background: '#181C27', border: '1px solid #252B3B', borderRadius: 10, padding: '11px 14px 11px 38px', color: '#F0F2F5', fontSize: 14, outline: 'none', boxSizing: 'border-box' },
  status:      { textAlign: 'center', padding: 48, color: '#6B7280' },
  emptyState:  { textAlign: 'center', padding: 64, background: '#181C27', border: '1px solid #252B3B', borderRadius: 16 },
  list:        { display: 'flex', flexDirection: 'column', gap: 10 },
  card:        { background: '#181C27', border: '1px solid #252B3B', borderRadius: 14, padding: '16px 20px', display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' },
  avatar:      { width: 46, height: 46, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, fontSize: 16, color: '#fff', flexShrink: 0, letterSpacing: '-0.5px' },
  contactName: { fontWeight: 700, fontSize: 16, color: '#F0F2F5', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' },
  contactTitle:{ color: '#9CA3AF', fontSize: 13, marginTop: 2 },
  tags:        { display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 8 },
  tag:         { display: 'inline-flex', alignItems: 'center', gap: 5, background: '#1E2435', border: '1px solid #252B3B', borderRadius: 8, padding: '3px 9px', fontSize: 12, color: '#9CA3AF' },
  actions:     { display: 'flex', gap: 6, flexShrink: 0, marginLeft: 'auto' },
  iconBtn:     { background: 'transparent', border: 'none', color: '#6B7280', cursor: 'pointer', padding: '7px 8px', borderRadius: 8, display: 'flex', alignItems: 'center' },
  primaryBtn:  { display: 'flex', alignItems: 'center', gap: 6, background: '#60A5FA', color: '#0F1117', border: 'none', borderRadius: 10, padding: '10px 18px', fontWeight: 700, fontSize: 14, cursor: 'pointer' },
  ghostBtn:    { background: 'transparent', border: '1px solid #252B3B', color: '#9CA3AF', borderRadius: 10, padding: '10px 18px', fontWeight: 600, fontSize: 14, cursor: 'pointer' },
  dangerBtn:   { background: '#EF444420', color: '#EF4444', border: '1px solid #EF444440', borderRadius: 10, padding: '10px 20px', fontWeight: 700, fontSize: 14, cursor: 'pointer' },
  detailLabel: { fontSize: 11, fontWeight: 700, color: '#6B7280', letterSpacing: '0.5px', textTransform: 'uppercase', marginBottom: 10 },
  badge:       { background: '#60A5FA20', color: '#60A5FA', border: '1px solid #60A5FA40', borderRadius: 6, padding: '2px 8px', fontSize: 11, fontWeight: 700 },
}
