import { useCallback, useEffect, useState } from 'react'
import type { Customer, Site, SiteRequest } from '../types'
import {
  createSite,
  deleteSite,
  fetchCustomers,
  fetchSites,
  updateSite,
} from '../api/client'
import './Management.css'

interface SiteFormState {
  name: string
  address: string
  customerId: string
}

const emptyForm: SiteFormState = { name: '', address: '', customerId: '' }

export default function SiteManagement() {
  const [sites, setSites] = useState<Site[]>([])
  const [customers, setCustomers] = useState<Customer[]>([])
  const [form, setForm] = useState<SiteFormState>(emptyForm)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let active = true

    fetchSites()
      .then(data => {
        if (active) {
          setSites(data)
          setLoading(false)
        }
      })
      .catch(err => {
        if (active) {
          setError(err instanceof Error ? err.message : 'Failed to load sites')
          setLoading(false)
        }
      })

    fetchCustomers()
      .then(data => {
        if (active) {
          setCustomers(data)
        }
      })
      .catch(err => {
        if (active) {
          setError(err instanceof Error ? err.message : 'Failed to load customers')
        }
      })

    return () => {
      active = false
    }
  }, [])

  const refreshSites = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchSites()
      setSites(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load sites')
    } finally {
      setLoading(false)
    }
  }, [])

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.name.trim()) {
      setError('Site name is required')
      return
    }
    if (!form.address.trim()) {
      setError('Site address is required')
      return
    }
    if (!form.customerId) {
      setError('A customer must be selected')
      return
    }

    setSaving(true)
    setError(null)
    try {
      const request: SiteRequest = {
        name: form.name.trim(),
        address: form.address.trim(),
        customerId: Number(form.customerId),
      }
      if (editingId !== null) {
        await updateSite(editingId, request)
      } else {
        await createSite(request)
      }
      setForm(emptyForm)
      setEditingId(null)
      await refreshSites()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save site')
    } finally {
      setSaving(false)
    }
  }

  const handleEdit = (site: Site) => {
    setEditingId(site.id)
    setForm({
      name: site.name,
      address: site.address,
      customerId: String(site.customerId),
    })
    setError(null)
  }

  const handleCancelEdit = () => {
    setEditingId(null)
    setForm(emptyForm)
    setError(null)
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this site?')) {
      return
    }
    setError(null)
    try {
      await deleteSite(id)
      await refreshSites()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete site')
    }
  }

  return (
    <div className="management-page">
      <h2>Site Management</h2>

      {error && <div className="error-banner">{error}</div>}

      <form className="management-form" onSubmit={handleSubmit}>
        <h3>{editingId !== null ? 'Edit Site' : 'Add Site'}</h3>
        <div className="form-row">
          <input
            type="text"
            name="name"
            placeholder="Site name"
            value={form.name}
            onChange={handleInputChange}
          />
        </div>
        <div className="form-row" style={{ marginTop: 8 }}>
          <input
            type="text"
            name="address"
            placeholder="Site address"
            value={form.address}
            onChange={handleInputChange}
          />
        </div>
        <div className="form-row" style={{ marginTop: 8 }}>
          <select
            name="customerId"
            value={form.customerId}
            onChange={handleInputChange}
          >
            <option value="">Select a customer...</option>
            {customers.map(customer => (
              <option key={customer.id} value={customer.id}>
                {customer.name}
              </option>
            ))}
          </select>
          <button type="submit" disabled={saving}>
            {saving ? 'Saving...' : editingId !== null ? 'Update' : 'Add'}
          </button>
          {editingId !== null && (
            <button type="button" className="secondary" onClick={handleCancelEdit}>
              Cancel
            </button>
          )}
        </div>
      </form>

      {loading ? (
        <p className="status-text">Loading sites...</p>
      ) : sites.length === 0 ? (
        <p className="status-text">No sites found. Add your first site above.</p>
      ) : (
        <table className="management-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Address</th>
              <th>Customer</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {sites.map(site => (
              <tr key={site.id}>
                <td>{site.id}</td>
                <td>{site.name}</td>
                <td>{site.address}</td>
                <td>{site.customerName}</td>
                <td className="actions">
                  <button className="secondary" onClick={() => handleEdit(site)}>
                    Edit
                  </button>
                  <button className="danger" onClick={() => handleDelete(site.id)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}