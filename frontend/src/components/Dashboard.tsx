import { useCallback, useEffect, useState } from 'react'
import { fetchDashboardSummary, login } from '../api/client'
import type { DashboardSummary, StatusCount, OverdueWorkOrder } from '../types'
import './Dashboard.css'

const STORAGE_KEY = 'keystone_dashboard_token'

export default function Dashboard() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [token, setToken] = useState<string | null>(() => {
    const stored = localStorage.getItem(STORAGE_KEY)
    return stored ? JSON.parse(stored).token : null
  })
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loggingIn, setLoggingIn] = useState(false)

  const loadDashboard = useCallback(async () => {
    if (!token) {
      setLoading(false)
      return
    }
    setLoading(true)
    setError(null)
    try {
      const data = await fetchDashboardSummary()
      setSummary(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load dashboard')
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    loadDashboard()
  }, [loadDashboard])

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoggingIn(true)
    setError(null)
    try {
      const result = await login(email, password)
      const newToken = result.token
      setToken(newToken)
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ token: newToken, role: result.role }))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed')
    } finally {
      setLoggingIn(false)
    }
  }

  const handleLogout = () => {
    setToken(null)
    setSummary(null)
    localStorage.removeItem(STORAGE_KEY)
  }

  const formatDate = (value: string | null) => {
    if (!value) return 'N/A'
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return 'N/A'
    return date.toLocaleDateString()
  }

  if (!token) {
    return (
      <div className="dashboard-page">
        <h2>Dashboard Login</h2>
        <p className="status-text">Please sign in with a Manager or Admin account to view the dashboard.</p>
        <form className="dashboard-login" onSubmit={handleLogin}>
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={e => setEmail(e.target.value)}
            required
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />
          <button type="submit" disabled={loggingIn}>
            {loggingIn ? 'Signing in...' : 'Sign In'}
          </button>
        </form>
        {error && <div className="error-banner">{error}</div>}
      </div>
    )
  }

  if (loading) {
    return <div className="dashboard-page"><p className="status-text">Loading dashboard...</p></div>
  }

  if (error) {
    return (
      <div className="dashboard-page">
        <div className="error-banner">{error}</div>
        {error.includes('403') && (
          <p className="status-text">You do not have permission to view the dashboard. Manager or Admin role required.</p>
        )}
        <button className="secondary" onClick={handleLogout}>Sign Out</button>
      </div>
    )
  }

  if (!summary) {
    return <div className="dashboard-page"><p className="status-text">No data available.</p></div>
  }

  return (
    <div className="dashboard-page">
      <div className="dashboard-header">
        <h2>Dashboard</h2>
        <button className="secondary" onClick={handleLogout}>Sign Out</button>
      </div>

      <div className="dashboard-cards">
        <div className="dashboard-card">
          <div className="dashboard-card-value">{summary.totalWorkOrders}</div>
          <div className="dashboard-card-label">Total Work Orders</div>
        </div>
        {summary.statusCounts.map((sc: StatusCount) => (
          <div className="dashboard-card" key={sc.status}>
            <div className="dashboard-card-value">{sc.count}</div>
            <div className="dashboard-card-label">{sc.status}</div>
          </div>
        ))}
      </div>

      <div className="dashboard-section">
        <h3>SLA Compliance</h3>
        <div className="dashboard-cards">
          <div className="dashboard-card">
            <div className="dashboard-card-value">{summary.slaCompliance.totalWithSla}</div>
            <div className="dashboard-card-label">Total with SLA</div>
          </div>
          <div className="dashboard-card">
            <div className="dashboard-card-value dashboard-card-success">{summary.slaCompliance.compliant}</div>
            <div className="dashboard-card-label">Compliant</div>
          </div>
          <div className="dashboard-card">
            <div className="dashboard-card-value dashboard-card-danger">{summary.slaCompliance.breached}</div>
            <div className="dashboard-card-label">Breached</div>
          </div>
          <div className="dashboard-card">
            <div className="dashboard-card-value">{summary.slaCompliance.complianceRate.toFixed(1)}%</div>
            <div className="dashboard-card-label">Compliance Rate</div>
          </div>
        </div>
      </div>

      <div className="dashboard-section">
        <h3>Overdue Work Orders {(summary.overdueWorkOrders ?? []).length}</h3>
        {(summary.overdueWorkOrders ?? []).length === 0 ? (
          <p className="status-text">No overdue work orders.</p>
        ) : (
          <table className="dashboard-table">
            <thead>
              <tr>
                <th>Code</th>
                <th>Title</th>
                <th>Status</th>
                <th>Due Date</th>
                <th>SLA Due Date</th>
                <th>Overdue Type</th>
              </tr>
            </thead>
            <tbody>
              {(summary.overdueWorkOrders ?? []).map((wo: OverdueWorkOrder) => (
                <tr key={wo.id}>
                  <td>{wo.code}</td>
                  <td>{wo.title}</td>
                  <td>{wo.status}</td>
                  <td>{formatDate(wo.dueDate)}</td>
                  <td>{formatDate(wo.slaDueDate)}</td>
                  <td>
                    <span className={wo.overdueType === 'SLA Breach' ? 'dashboard-badge-danger' : 'dashboard-badge-warning'}>
                      {wo.overdueType}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="dashboard-section">
        <h3>Work Orders by Technician</h3>
        {(summary.technicianBreakdown ?? []).length === 0 ? (
          <p className="status-text">No technician assignments found.</p>
        ) : (
          <table className="dashboard-table">
            <thead>
              <tr>
                <th>Technician</th>
                <th>Work Orders</th>
              </tr>
            </thead>
            <tbody>
              {(summary.technicianBreakdown ?? []).map((row) => (
                <tr key={row.status}>
                  <td>{row.status}</td>
                  <td>{row.count}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="dashboard-section">
        <h3>Work Orders by Site</h3>
        {(summary.siteBreakdown ?? []).length === 0 ? (
          <p className="status-text">No site assignments found.</p>
        ) : (
          <table className="dashboard-table">
            <thead>
              <tr>
                <th>Site</th>
                <th>Work Orders</th>
              </tr>
            </thead>
            <tbody>
              {(summary.siteBreakdown ?? []).map((row) => (
                <tr key={row.status}>
                  <td>{row.status}</td>
                  <td>{row.count}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
