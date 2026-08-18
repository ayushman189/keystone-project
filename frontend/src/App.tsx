import { useState, useEffect } from 'react'
import CustomerManagement from './components/CustomerManagement'
import SiteManagement from './components/SiteManagement'
import WorkOrderKanban from './components/WorkOrderKanban'
import Dashboard from './components/Dashboard'
import { login } from './api/client'
import './App.css'

type View = 'customers' | 'sites' | 'work-orders' | 'dashboard' | 'login'

interface StoredAuth {
  token: string
  role: string
}

function App() {
  const [view, setView] = useState<View>('login')
  const [auth, setAuth] = useState<StoredAuth | null>(() => {
    const stored = localStorage.getItem('keystone_auth')
    return stored ? JSON.parse(stored) : null
  })
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loggingIn, setLoggingIn] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (auth?.token) {
      localStorage.setItem('keystone_auth', JSON.stringify(auth))
    } else {
      localStorage.removeItem('keystone_auth')
    }
  }, [auth])

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoggingIn(true)
    setError(null)
    try {
      const result = await login(email, password)
      setAuth({ token: result.token, role: result.role })
      setView('work-orders')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed')
    } finally {
      setLoggingIn(false)
    }
  }

  const handleLogout = () => {
    setAuth(null)
    setView('login')
    setEmail('')
    setPassword('')
  }

  const role = auth?.role

  const canViewCustomers = role === 'MANAGER' || role === 'ADMIN' || role === 'DISPATCHER'
  const canViewSites = role === 'MANAGER' || role === 'ADMIN' || role === 'DISPATCHER'
  const canViewWorkOrders = role === 'MANAGER' || role === 'ADMIN' || role === 'DISPATCHER' || role === 'TECHNICIAN'
  const canViewDashboard = role === 'MANAGER' || role === 'ADMIN'
  const canViewCustomerPortal = role === 'CUSTOMER'

  if (view === 'login' || !auth?.token) {
    return (
      <div className="app">
        <div className="login-page">
          <h1>Project Keystone</h1>
          <p className="status-text">Sign in to continue</p>
          <form className="login-form" onSubmit={handleLogin}>
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
      </div>
    )
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Project Keystone</h1>
        <nav className="app-nav">
          {canViewCustomers && (
            <button
              className={view === 'customers' ? 'active' : ''}
              onClick={() => setView('customers')}
            >
              Customers
            </button>
          )}
          {canViewSites && (
            <button
              className={view === 'sites' ? 'active' : ''}
              onClick={() => setView('sites')}
            >
              Sites
            </button>
          )}
          {canViewWorkOrders && (
            <button
              className={view === 'work-orders' ? 'active' : ''}
              onClick={() => setView('work-orders')}
            >
              Work Orders
            </button>
          )}
          {canViewCustomerPortal && (
            <button
              className={view === 'work-orders' ? 'active' : ''}
              onClick={() => setView('work-orders')}
            >
              My Requests
            </button>
          )}
          {canViewDashboard && (
            <button
              className={view === 'dashboard' ? 'active' : ''}
              onClick={() => setView('dashboard')}
            >
              Dashboard
            </button>
          )}
          <button className="secondary" onClick={handleLogout}>Sign Out</button>
        </nav>
      </header>

      <main className="app-main">
        {view === 'customers' && canViewCustomers && <CustomerManagement />}
        {view === 'sites' && canViewSites && <SiteManagement />}
        {view === 'work-orders' && (canViewWorkOrders || canViewCustomerPortal) && <WorkOrderKanban userRole={role} />}
        {view === 'dashboard' && canViewDashboard && <Dashboard />}
      </main>
    </div>
  )
}

export default App
