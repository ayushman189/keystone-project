export interface StatusCount {
  status: string
  count: number
}

export interface OverdueWorkOrder {
  id: number
  code: string
  title: string
  status: string
  dueDate: string | null
  slaDueDate: string | null
  overdueType: string
}

export interface SlaCompliance {
  totalWithSla: number
  compliant: number
  breached: number
  complianceRate: number
}

export interface DashboardSummary {
  totalWorkOrders: number
  statusCounts: StatusCount[]
  overdueWorkOrders: OverdueWorkOrder[]
  slaCompliance: SlaCompliance
  technicianBreakdown: StatusCount[]
  siteBreakdown: StatusCount[]
}
