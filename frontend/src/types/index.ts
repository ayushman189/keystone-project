export interface Customer {
  id: number
  name: string
}

export interface CustomerRequest {
  name: string
}

export interface Site {
  id: number
  name: string
  address: string
  customerId: number
  customerName: string
}

export interface SiteRequest {
  name: string
  address: string
  customerId: number
}

export interface WorkOrder {
  id: number
  code: string
  title: string
  description: string | null
  priority: string
  status: string
  dueDate: string | null
  slaDueDate: string | null
  customerId: number
  customerName: string
  siteId: number
  siteName: string
  assigneeId: number | null
  assigneeName: string | null
}

export interface WorkOrderRequest {
  code: string
  title: string
  description: string | null
  priority: string
  status: string
  dueDate: string | null
  slaDueDate: string | null
  customerId: number
  siteId: number
  assigneeId: number | null
}

export interface User {
  id: number
  name: string
  email: string
  role: string
}

export interface Notification {
  id: number
  workOrderId: number
  workOrderCode: string
  type: string
  message: string
  createdAt: string
  read: boolean
}

export * from './dashboard'
