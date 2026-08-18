import { useCallback, useEffect, useMemo, useState } from 'react'
import type { Customer, Site, User, WorkOrder, WorkOrderRequest } from '../types'
import {
  createWorkOrder,
  deleteWorkOrder,
  fetchCustomers,
  fetchSites,
  fetchTechnicians,
  fetchWorkOrders,
  fetchWorkOrdersByAssignee,
  fetchMyWorkOrders,
  updateWorkOrder,
  fetchNotifications,
  markNotificationAsRead,
} from '../api/client'
import './WorkOrderKanban.css'

interface WorkOrderKanbanProps {
  userRole?: string
}

interface WorkOrderFormState {
  code: string
  title: string
  description: string
  priority: string
  status: string
  dueDate: string
  slaDueDate: string
  customerId: string
  siteId: string
  assigneeId: string
}

const emptyForm: WorkOrderFormState = {
  code: '',
  title: '',
  description: '',
  priority: 'medium',
  status: '',
  dueDate: '',
  slaDueDate: '',
  customerId: '',
  siteId: '',
  assigneeId: '',
}

export default function WorkOrderKanban({ userRole }: WorkOrderKanbanProps) {
  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([])
  const [customers, setCustomers] = useState<Customer[]>([])
  const [sites, setSites] = useState<Site[]>([])
  const [technicians, setTechnicians] = useState<User[]>([])
  const [myWorkOrdersOnly, setMyWorkOrdersOnly] = useState(false)
  const [currentTechnicianId, setCurrentTechnicianId] = useState<number | null>(null)
  const [form, setForm] = useState<WorkOrderFormState>(emptyForm)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [page, setPage] = useState(1)
  const [pageSize, _setPageSize] = useState(10)
  const [total, setTotal] = useState(0)
  const [statusFilter, setStatusFilter] = useState('')
  const [priorityFilter, setPriorityFilter] = useState('')
  const [customerFilter, setCustomerFilter] = useState('')
  const [siteFilter, setSiteFilter] = useState('')
  const [searchFilter, setSearchFilter] = useState('')
  const [notifications, setNotifications] = useState<Array<{ id: number; message: string; read: boolean }>>([])
  const [showNotifications, setShowNotifications] = useState(false)

  const isCustomer = userRole === 'CUSTOMER'
  const isTechnician = userRole === 'TECHNICIAN'
  const canManage = userRole === 'MANAGER' || userRole === 'ADMIN' || userRole === 'DISPATCHER'

  useEffect(() => {
    let active = true

    const loadWorkOrders = async () => {
      try {
        let data: WorkOrder[]
        if (isCustomer || isTechnician) {
          data = await fetchMyWorkOrders()
        } else {
          data = await fetchWorkOrders()
        }
        if (active) {
          setWorkOrders(data)
          setTotal(data.length)
          setLoading(false)
        }
      } catch (err) {
        if (active) {
          setError(err instanceof Error ? err.message : 'Failed to load work orders')
          setLoading(false)
        }
      }
    }

    loadWorkOrders()

    return () => {
      active = false
    }
  }, [isCustomer, isTechnician])

  useEffect(() => {
    let active = true

    Promise.all([fetchCustomers(), fetchSites(), fetchTechnicians()])
      .then(([custData, siteData, techData]) => {
        if (active) {
          setCustomers(custData)
          setSites(siteData)
          setTechnicians(techData)
        }
      })
      .catch(() => {
        if (active) {
        }
      })

    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    let active = true
    fetchNotifications()
      .then(data => {
        if (active) {
          setNotifications(data.map(n => ({ id: n.id, message: n.message, read: n.read })))
        }
      })
      .catch(() => {
        // Non-fatal
      })
    return () => {
      active = false
    }
  }, [workOrders])

  const refreshWorkOrders = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      let data: WorkOrder[]
      if (isCustomer || isTechnician) {
        data = await fetchMyWorkOrders()
      } else if (myWorkOrdersOnly && currentTechnicianId) {
        data = await fetchWorkOrdersByAssignee(currentTechnicianId)
      } else {
        data = await fetchWorkOrders()
      }
      setWorkOrders(data)
      setTotal(data.length)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load work orders')
    } finally {
      setLoading(false)
    }
  }, [isCustomer, isTechnician, myWorkOrdersOnly, currentTechnicianId])

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target
    setSearchFilter(value)
    setPage(1) // Reset to first page on search
    refreshWorkOrders()
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.code.trim() || !form.title.trim() || !form.priority || !form.status || !form.customerId || !form.siteId) {
      setError('Please fill in all required fields')
      return
    }

    setSaving(true)
    setError(null)
    try {
      const request: WorkOrderRequest = {
        code: form.code.trim(),
        title: form.title.trim(),
        description: form.description.trim() || null,
        priority: form.priority,
        status: form.status.trim(),
        dueDate: form.dueDate || null,
        slaDueDate: form.slaDueDate || null,
        customerId: Number(form.customerId),
        siteId: Number(form.siteId),
        assigneeId: form.assigneeId ? Number(form.assigneeId) : null,
      }
      if (editingId !== null) {
        await updateWorkOrder(editingId, request)
      } else {
        await createWorkOrder(request)
      }
      setForm(emptyForm)
      setEditingId(null)
      await refreshWorkOrders()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save work order')
    } finally {
      setSaving(false)
    }
  }

  const handleEdit = (workOrder: WorkOrder) => {
    setEditingId(workOrder.id)
    setForm({
      code: workOrder.code,
      title: workOrder.title,
      description: workOrder.description ?? '',
      priority: workOrder.priority,
      status: workOrder.status,
      dueDate: workOrder.dueDate ? workOrder.dueDate.slice(0, 10) : '',
      slaDueDate: workOrder.slaDueDate ? workOrder.slaDueDate.slice(0, 10) : '',
      customerId: String(workOrder.customerId),
      siteId: String(workOrder.siteId),
      assigneeId: workOrder.assigneeId ? String(workOrder.assigneeId) : '',
    })
    setError(null)
  }

  const handleCancelEdit = () => {
    setEditingId(null)
    setForm(emptyForm)
    setError(null)
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this work order?')) {
      return
    }
    setError(null)
    try {
      await deleteWorkOrder(id)
      await refreshWorkOrders()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete work order')
    }
  }

  const totalPages = Math.ceil(total / pageSize)

  const columns = useMemo(() => {
    const statuses: string[] = []
    for (const wo of workOrders) {
      if (wo.status && !statuses.includes(wo.status)) {
        statuses.push(wo.status)
      }
    }
    return statuses.map(status => ({
      status,
      items: workOrders.filter(wo => wo.status === status),
    }))
  }, [workOrders])

  const formatDate = (value: string | null) => {
    if (!value) return null
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return null
    return date.toLocaleDateString()
  }

  const isOverdue = (workOrder: WorkOrder) => {
    if (!workOrder.slaDueDate || workOrder.status === 'Done' || workOrder.status === 'Cancelled') {
      return false
    }
    return new Date(workOrder.slaDueDate) < new Date()
  }

  const handleMarkNotificationRead = async (id: number) => {
    await markNotificationAsRead(id)
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n))
  }

  const unreadCount = notifications.filter(n => !n.read).length

  return (
    <div className="kanban-page">
      <div className="kanban-header">
        <h2>Work Order Board</h2>
        <button className="notification-toggle" onClick={() => setShowNotifications(v => !v)}>
          Notifications {unreadCount > 0 && <span className="notification-badge">{unreadCount}</span>}
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {showNotifications && (
        <div className="notifications-panel">
          <h4>Notifications</h4>
          {notifications.length === 0 && <p className="status-text">No notifications.</p>}
          {notifications.map(n => (
            <div key={n.id} className={`notification-item ${n.read ? 'read' : 'unread'}`}>
              <span>{n.message}</span>
              {!n.read && (
                <button className="secondary" onClick={() => handleMarkNotificationRead(n.id)}>Mark read</button>
              )}
            </div>
          ))}
        </div>
      )}

      <div className="filters-bar">
        <select
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value)
            setPage(1)
            refreshWorkOrders()
          }}
        >
          <option value="">All Statuses</option>
          <option value="Open">Open</option>
          <option value="In Progress">In Progress</option>
          <option value="Done">Done</option>
        </select>

        <select
          value={priorityFilter}
          onChange={(e) => {
            setPriorityFilter(e.target.value)
            setPage(1)
            refreshWorkOrders()
          }}
        >
          <option value="">All Priorities</option>
          <option value="low">Low</option>
          <option value="medium">Medium</option>
          <option value="high">High</option>
        </select>

        <select
          value={customerFilter}
          onChange={(e) => {
            setCustomerFilter(e.target.value)
            setPage(1)
            refreshWorkOrders()
          }}
        >
          <option value="">All Customers</option>
          {customers.map(c => (
            <option key={c.id} value={c.id.toString()}>
              {c.name}
            </option>
          ))}
        </select>

        <select
          value={siteFilter}
          onChange={(e) => {
            setSiteFilter(e.target.value)
            setPage(1)
            refreshWorkOrders()
          }}
        >
          <option value="">All Sites</option>
          {sites.map(s => (
            <option key={s.id} value={s.id.toString()}>
              {s.name}
            </option>
          ))}
        </select>

        <input
          type="text"
          placeholder="Search code or title..."
          value={searchFilter}
          onChange={handleSearchChange}
        />

        <select
          value={currentTechnicianId ?? ''}
          onChange={(e) => {
            const val = e.target.value
            setCurrentTechnicianId(val ? Number(val) : null)
            setMyWorkOrdersOnly(!!val)
            setPage(1)
            refreshWorkOrders()
          }}
        >
          <option value="">All Work Orders</option>
          {technicians.map(t => (
            <option key={t.id} value={t.id}>
              My Work Orders: {t.name}
            </option>
          ))}
        </select>
      </div>

      {canManage && (
        <form className="management-form" onSubmit={handleSubmit}>
          <h3>{editingId !== null ? 'Edit Work Order' : 'Add Work Order'}</h3>
        <div className="form-row">
          <input
            type="text"
            name="code"
            placeholder="Code"
            value={form.code}
            onChange={handleInputChange}
            required
          />
          <input
            type="text"
            name="title"
            placeholder="Title"
            value={form.title}
            onChange={handleInputChange}
            required
          />
        </div>
        <div className="form-row">
          <input
            type="text"
            name="description"
            placeholder="Description"
            value={form.description}
            onChange={handleInputChange}
          />
          <select name="priority" value={form.priority} onChange={handleInputChange} required>
            <option value="low">Low</option>
            <option value="medium">Medium</option>
            <option value="high">High</option>
          </select>
        </div>
        <div className="form-row">
          <input
            type="text"
            name="status"
            placeholder="Status (e.g. Open, In Progress, Done)"
            value={form.status}
            onChange={handleInputChange}
            required
          />
          <div className="form-field">
            <label htmlFor="dueDate">Due Date</label>
            <input
              type="date"
              id="dueDate"
              name="dueDate"
              value={form.dueDate}
              onChange={handleInputChange}
            />
          </div>
        </div>
        <div className="form-row">
          <div className="form-field">
            <label htmlFor="slaDueDate">SLA Due Date</label>
            <input
              type="date"
              id="slaDueDate"
              name="slaDueDate"
              value={form.slaDueDate}
              onChange={handleInputChange}
            />
          </div>
        </div>
        <div className="form-row">
          <select name="customerId" value={form.customerId} onChange={handleInputChange} required>
            <option value="">Select Customer</option>
            {customers.map(c => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          <select name="siteId" value={form.siteId} onChange={handleInputChange} required>
            <option value="">Select Site</option>
            {sites.map(s => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </div>
        <div className="form-row">
          <select name="assigneeId" value={form.assigneeId} onChange={handleInputChange}>
            <option value="">Unassigned</option>
            {technicians.map(t => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
          <button type="submit" disabled={saving}>
            {saving ? 'Saving...' : editingId !== null ? 'Update' : 'Add Work Order'}
          </button>
          {editingId !== null && (
            <button type="button" className="secondary" onClick={handleCancelEdit}>
              Cancel
            </button>
          )}
        </div>
      </form>
      )}

      {loading ? (
        <p className="status-text">Loading work orders...</p>
      ) : workOrders.length === 0 ? (
        <p className="status-text">No work orders found. Add your first work order above.</p>
      ) : (
        <div className="kanban-board">
          {columns.map(column => (
            <div className="kanban-column" key={column.status}>
              <div className="kanban-column-header">
                <span className="kanban-column-title">{column.status}</span>
                <span className="kanban-column-count">{column.items.length}</span>
              </div>
              <div className="kanban-column-body">
                {column.items.map(workOrder => (
                  <div className="kanban-card" key={workOrder.id}>
                    <div className="kanban-card-top">
                      <span className="kanban-card-code">{workOrder.code}</span>
                      <span className={`kanban-priority priority-${workOrder.priority.toLowerCase()}`}>
                        {workOrder.priority}
                      </span>
                    </div>
                    <h3 className="kanban-card-title">{workOrder.title}</h3>
                    <div className="kanban-card-meta">
                      <span>Customer: {workOrder.customerName}</span>
                      <span>Site: {workOrder.siteName}</span>
                      {workOrder.assigneeName && <span>Assignee: {workOrder.assigneeName}</span>}
                      {formatDate(workOrder.dueDate) && (
                        <span>Due: {formatDate(workOrder.dueDate)}</span>
                      )}
                      {formatDate(workOrder.slaDueDate) && (
                        <span className={isOverdue(workOrder) ? 'sla-overdue' : ''}>
                          SLA Due: {formatDate(workOrder.slaDueDate)}
                          {isOverdue(workOrder) && ' (SLA BREACHED)'}
                        </span>
                      )}
                    </div>
                    <div className="kanban-card-actions">
                      {canManage && (
                        <>
                          <button className="secondary" onClick={() => handleEdit(workOrder)}>
                            Edit
                          </button>
                          <button className="danger" onClick={() => handleDelete(workOrder.id)}>
                            Delete
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
          <div className="pagination-bar">
            <button
              onClick={() => setPage(Math.max(1, page - 1))}
              disabled={page === 1}
            >
              Prev
            </button>
            <span>Page {page} of {totalPages}</span>
            <button
              onClick={() => setPage(Math.min(totalPages, page + 1))}
              disabled={page === totalPages}
            >
              Next
            </button>
          </div>
        </div>
      )}
    </div>
  )
}