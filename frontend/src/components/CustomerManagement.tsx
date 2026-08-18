import { useCallback, useEffect, useState } from 'react'
import type { Customer, CustomerRequest } from '../types'
import { createCustomer, deleteCustomer, fetchCustomers, updateCustomer } from '../api/client'
import './Management.css'

interface CustomerFormState {
  name: string
}

const emptyForm: CustomerFormState = { name: '' }

export default function CustomerManagement() {
  const [customers, setCustomers] = useState<Customer[]>([])
  const [form, setForm] = useState<CustomerFormState>(emptyForm)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let active = true

    fetchCustomers()
      .then(data => {
        if (active) {
          setCustomers(data)
          setLoading(false)
        }
      })
      .catch(err => {
        if (active) {
          setError(err instanceof Error ? err.message : 'Failed to load customers')
          setLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [])

  const refreshCustomers = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchCustomers()
      setCustomers(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load customers')
    } finally {
      setLoading(false)
    }
  }, [])

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.name.trim()) {
      setError('Customer name is required')
      return
    }

    setSaving(true)
    setError(null)
    try {
      const request: CustomerRequest = { name: form.name.trim() }
      if (editingId !== null) {
        await updateCustomer(editingId, request)
      } else {
        await createCustomer(request)
      }
      setForm(emptyForm)
      setEditingId(null)
      await refreshCustomers()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save customer')
    } finally {
      setSaving(false)
    }
  }

  const handleEdit = (customer: Customer) => {
    setEditingId(customer.id)
    setForm({ name: customer.name })
    setError(null)
  }

  const handleCancelEdit = () => {
    setEditingId(null)
    setForm(emptyForm)
    setError(null)
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this customer?')) {
      return
    }
    setError(null)
    try {
      await deleteCustomer(id)
      await refreshCustomers()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete customer')
    }
  }

  return (
    <div className="management-page">
      <h2>Customer Management</h2>

      {error && <div className="error-banner">{error}</div>}

      <form className="management-form" onSubmit={handleSubmit}>
        <h3>{editingId !== null ? 'Edit Customer' : 'Add Customer'}</h3>
        <div className="form-row">
          <input
            type="text"
            name="name"
            placeholder="Customer name"
            value={form.name}
            onChange={handleInputChange}
          />
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
        <p className="status-text">Loading customers...</p>
      ) : customers.length === 0 ? (
        <p className="status-text">No customers found. Add your first customer above.</p>
      ) : (
        <table className="management-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {customers.map(customer => (
              <tr key={customer.id}>
                <td>{customer.id}</td>
                <td>{customer.name}</td>
                <td className="actions">
                  <button className="secondary" onClick={() => handleEdit(customer)}>
                    Edit
                  </button>
                  <button className="danger" onClick={() => handleDelete(customer.id)}>
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