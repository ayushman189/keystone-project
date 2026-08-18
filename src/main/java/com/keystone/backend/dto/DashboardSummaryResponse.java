package com.keystone.backend.dto;

import java.util.List;

public class DashboardSummaryResponse {

    private long totalWorkOrders;
    private List<StatusCountResponse> statusCounts;
    private List<OverdueWorkOrderResponse> overdueWorkOrders;
    private SlaComplianceResponse slaCompliance;
    private List<StatusCountResponse> technicianBreakdown;
    private List<StatusCountResponse> siteBreakdown;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(long totalWorkOrders, List<StatusCountResponse> statusCounts, List<OverdueWorkOrderResponse> overdueWorkOrders, SlaComplianceResponse slaCompliance, List<StatusCountResponse> technicianBreakdown, List<StatusCountResponse> siteBreakdown) {
        this.totalWorkOrders = totalWorkOrders;
        this.statusCounts = statusCounts;
        this.overdueWorkOrders = overdueWorkOrders;
        this.slaCompliance = slaCompliance;
        this.technicianBreakdown = technicianBreakdown;
        this.siteBreakdown = siteBreakdown;
    }

    public long getTotalWorkOrders() {
        return totalWorkOrders;
    }

    public void setTotalWorkOrders(long totalWorkOrders) {
        this.totalWorkOrders = totalWorkOrders;
    }

    public List<StatusCountResponse> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(List<StatusCountResponse> statusCounts) {
        this.statusCounts = statusCounts;
    }

    public List<OverdueWorkOrderResponse> getOverdueWorkOrders() {
        return overdueWorkOrders;
    }

    public void setOverdueWorkOrders(List<OverdueWorkOrderResponse> overdueWorkOrders) {
        this.overdueWorkOrders = overdueWorkOrders;
    }

    public SlaComplianceResponse getSlaCompliance() {
        return slaCompliance;
    }

    public void setSlaCompliance(SlaComplianceResponse slaCompliance) {
        this.slaCompliance = slaCompliance;
    }

    public List<StatusCountResponse> getTechnicianBreakdown() {
        return technicianBreakdown;
    }

    public void setTechnicianBreakdown(List<StatusCountResponse> technicianBreakdown) {
        this.technicianBreakdown = technicianBreakdown;
    }

    public List<StatusCountResponse> getSiteBreakdown() {
        return siteBreakdown;
    }

    public void setSiteBreakdown(List<StatusCountResponse> siteBreakdown) {
        this.siteBreakdown = siteBreakdown;
    }
}
