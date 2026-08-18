import type {
  Customer,
  CustomerRequest,
  Site,
  SiteRequest,
  User,
  WorkOrder,
  WorkOrderRequest,
  Notification,
  DashboardSummary,
} from '../types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let message = `Request failed with status ${response.status}`
    try {
      const errorBody = await response.json()
      if (errorBody?.message) {
        if (typeof errorBody.message === 'string') {
          message = errorBody.message
        } else {
          message = JSON.stringify(errorBody.message)
        }
      }
    } catch {
      // Ignore JSON parse errors, keep default message
    }
    throw new Error(message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

function authHeaders(): Record<string, string> {
  // Main app stores auth in 'keystone_auth' as JSON { token, role }
  const stored = localStorage.getItem('keystone_auth')
  if (stored) {
    try {
      const parsed = JSON.parse(stored)
      if (parsed?.token) {
        return { Authorization: `Bearer ${parsed.token}` }
      }
    } catch {
      // Ignore JSON parse errors, fall through
    }
  }

  // Dashboard stores its own token in 'keystone_dashboard_token' as JSON { token, role }
  const dashboardStored = localStorage.getItem('keystone_dashboard_token')
  if (dashboardStored) {
    try {
      const parsed = JSON.parse(dashboardStored)
      if (parsed?.token) {
        return { Authorization: `Bearer ${parsed.token}` }
      }
    } catch {
      // Ignore JSON parse errors, fall through
    }
  }

  // Legacy key
  const legacyToken = localStorage.getItem('keystone_token')
  if (legacyToken) {
    return { Authorization: `Bearer ${legacyToken}` }
  }

  return {}
}

// --- Customers ---

export async function fetchCustomers(): Promise<Customer[]> {
  const response = await fetch(`${API_BASE_URL}/customers`, {
    headers: authHeaders(),
  })
  return handleResponse<Customer[]>(response)
}

export async function createCustomer(request: CustomerRequest): Promise<Customer> {
  const response = await fetch(`${API_BASE_URL}/customers`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(request),
  })
  return handleResponse<Customer>(response)
}

export async function updateCustomer(id: number, request: CustomerRequest): Promise<Customer> {
  const response = await fetch(`${API_BASE_URL}/customers/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(request),
  })
  return handleResponse<Customer>(response)
}

export async function deleteCustomer(id: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/customers/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  return handleResponse<void>(response)
}

// --- Sites ---

export async function fetchSites(customerId?: number): Promise<Site[]> {
  const query = customerId ? `?customerId=${customerId}` : ''
  const response = await fetch(`${API_BASE_URL}/sites${query}`, {
    headers: authHeaders(),
  })
  return handleResponse<Site[]>(response)
}

export async function createSite(request: SiteRequest): Promise<Site> {
  const response = await fetch(`${API_BASE_URL}/sites`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(request),
  })
  return handleResponse<Site>(response)
}

export async function updateSite(id: number, request: SiteRequest): Promise<Site> {
  const response = await fetch(`${API_BASE_URL}/sites/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(request),
  })
  return handleResponse<Site>(response)
}

export async function deleteSite(id: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/sites/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  return handleResponse<void>(response)
}

// --- Work Orders ---

export async function fetchWorkOrders(): Promise<WorkOrder[]> {
  const response = await fetch(`${API_BASE_URL}/work-orders`, {
    headers: authHeaders(),
  })
  return handleResponse<WorkOrder[]>(response)
}

export async function createWorkOrder(request: WorkOrderRequest): Promise<WorkOrder> {
  const response = await fetch(`${API_BASE_URL}/work-orders`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(request),
  })
  return handleResponse<WorkOrder>(response)
}

export async function updateWorkOrder(id: number, request: WorkOrderRequest): Promise<WorkOrder> {
  const response = await fetch(`${API_BASE_URL}/work-orders/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(request),
  })
  return handleResponse<WorkOrder>(response)
}

export async function deleteWorkOrder(id: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/work-orders/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  })
  return handleResponse<void>(response)
}

// --- Technicians ---

export async function fetchTechnicians(): Promise<User[]> {
  const response = await fetch(`${API_BASE_URL}/users/technicians`, {
    headers: authHeaders(),
  })
  return handleResponse<User[]>(response)
}

export async function fetchWorkOrdersByAssignee(assigneeId: number): Promise<WorkOrder[]> {
  const response = await fetch(`${API_BASE_URL}/work-orders/assignee/${assigneeId}`, {
    headers: authHeaders(),
  })
  return handleResponse<WorkOrder[]>(response)
}

export async function assignTechnician(workOrderId: number, technicianId: number): Promise<WorkOrder> {
  const response = await fetch(`${API_BASE_URL}/work-orders/${workOrderId}/assign`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify({ technicianId }),
  })
  return handleResponse<WorkOrder>(response)
}

// --- Notifications ---

export async function fetchNotifications(): Promise<Notification[]> {
  const response = await fetch(`${API_BASE_URL}/notifications`, {
    headers: authHeaders(),
  })
  return handleResponse<Notification[]>(response)
}

export async function fetchUnreadNotifications(): Promise<Notification[]> {
  const response = await fetch(`${API_BASE_URL}/notifications/unread`, {
    headers: authHeaders(),
  })
  return handleResponse<Notification[]>(response)
}

export async function markNotificationAsRead(id: number): Promise<Notification> {
  const response = await fetch(`${API_BASE_URL}/notifications/${id}/read`, {
    method: 'PUT',
    headers: authHeaders(),
  })
  return handleResponse<Notification>(response)
}

// --- Auth ---

export async function login(email: string, password: string): Promise<{ token: string; role: string }> {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  return handleResponse<{ token: string; role: string }>(response)
}

// --- Reports ---

export async function fetchDashboardSummary(filters?: { status?: string; priority?: string; customerId?: number; siteId?: number; assigneeId?: number; search?: string }): Promise<DashboardSummary> {
  const params = new URLSearchParams()
  if (filters) {
    if (filters.status) params.set('status', filters.status)
    if (filters.priority) params.set('priority', filters.priority)
    if (filters.customerId != null) params.set('customerId', String(filters.customerId))
    if (filters.siteId != null) params.set('siteId', String(filters.siteId))
    if (filters.assigneeId != null) params.set('assigneeId', String(filters.assigneeId))
    if (filters.search) params.set('search', filters.search)
  }
  const query = params.toString()
  const response = await fetch(`${API_BASE_URL}/reports/summary${query ? '?' + query : ''}`, {
    headers: authHeaders(),
  })
  return handleResponse<DashboardSummary>(response)
}

// --- My Work Orders ---

export async function fetchMyWorkOrders(): Promise<WorkOrder[]> {
  const response = await fetch(`${API_BASE_URL}/my-work-orders`, {
    headers: authHeaders(),
  })
  return handleResponse<WorkOrder[]>(response)
}
