package com.keystone.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keystone.backend.dto.DashboardSummaryResponse;
import com.keystone.backend.dto.OverdueWorkOrderResponse;
import com.keystone.backend.dto.SlaComplianceResponse;
import com.keystone.backend.dto.StatusCountResponse;
import com.keystone.backend.entity.WorkOrder;
import com.keystone.backend.repository.WorkOrderRepository;

@Service
public class ReportService {

    private static final List<String> NON_BREACHABLE_STATUSES = List.of("Done", "Cancelled");

    private final WorkOrderRepository workOrderRepository;

    public ReportService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(String status, String priority, Long customerId, Long siteId, Long assigneeId, String search) {
        LocalDateTime now = LocalDateTime.now();

        List<WorkOrder> filteredWorkOrders = workOrderRepository.findByFilter(status, priority, customerId, siteId, assigneeId, search);
        long totalWorkOrders = filteredWorkOrders.size();

        java.util.Map<String, Long> statusMap = new java.util.HashMap<>();
        java.util.Map<String, Long> technicianMap = new java.util.HashMap<>();
        java.util.Map<String, Long> siteMap = new java.util.HashMap<>();

        for (WorkOrder wo : filteredWorkOrders) {
            String woStatus = wo.getStatus();
            statusMap.merge(woStatus, 1L, Long::sum);

            if (wo.getAssignee() != null && "TECHNICIAN".equals(wo.getAssignee().getRole())) {
                String techName = wo.getAssignee().getName();
                technicianMap.merge(techName, 1L, Long::sum);
            }

            if (wo.getSite() != null) {
                String siteName = wo.getSite().getName();
                siteMap.merge(siteName, 1L, Long::sum);
            }
        }

        List<StatusCountResponse> statusCounts = statusMap.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(e -> new StatusCountResponse(e.getKey(), e.getValue()))
                .toList();

        List<StatusCountResponse> technicianBreakdown = technicianMap.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(e -> new StatusCountResponse(e.getKey(), e.getValue()))
                .toList();

        List<StatusCountResponse> siteBreakdown = siteMap.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(e -> new StatusCountResponse(e.getKey(), e.getValue()))
                .toList();

        List<WorkOrder> overdueByDueDate = workOrderRepository.findByDueDateBeforeAndStatusNotIn(now, NON_BREACHABLE_STATUSES);
        List<WorkOrder> overdueBySla = workOrderRepository.findBySlaDueDateBeforeAndStatusNotIn(now, NON_BREACHABLE_STATUSES);

        java.util.Set<Long> overdueIds = new java.util.LinkedHashSet<>();
        List<OverdueWorkOrderResponse> overdueWorkOrders = new ArrayList<>();
        for (WorkOrder wo : overdueByDueDate) {
            if (matchesFilter(wo, status, priority, customerId, siteId, assigneeId, search)) {
                String type = (wo.getSlaDueDate() != null && wo.getSlaDueDate().isBefore(now)) ? "SLA Breach" : "Due Date Overdue";
                overdueWorkOrders.add(toOverdueResponse(wo, type));
                overdueIds.add(wo.getId());
            }
        }
        for (WorkOrder wo : overdueBySla) {
            if (!overdueIds.contains(wo.getId()) && matchesFilter(wo, status, priority, customerId, siteId, assigneeId, search)) {
                if (wo.getDueDate() == null || !wo.getDueDate().isBefore(now)) {
                    overdueWorkOrders.add(toOverdueResponse(wo, "SLA Breach"));
                }
            }
        }

        List<WorkOrder> slaWorkOrders = filteredWorkOrders.stream()
                .filter(wo -> wo.getSlaDueDate() != null)
                .toList();
        long totalWithSla = slaWorkOrders.size();
        long compliant = slaWorkOrders.stream()
                .filter(wo -> wo.getSlaDueDate().isAfter(now) || NON_BREACHABLE_STATUSES.contains(wo.getStatus()))
                .count();
        long breached = totalWithSla - compliant;
        double complianceRate = totalWithSla > 0 ? (double) compliant / totalWithSla * 100 : 100.0;

        SlaComplianceResponse slaCompliance = new SlaComplianceResponse(totalWithSla, compliant, breached, complianceRate);

        return new DashboardSummaryResponse(totalWorkOrders, statusCounts, overdueWorkOrders, slaCompliance, technicianBreakdown, siteBreakdown);
    }

    private boolean matchesFilter(WorkOrder wo, String status, String priority, Long customerId, Long siteId, Long assigneeId, String search) {
        if (status != null && !status.isBlank() && !status.equals(wo.getStatus())) return false;
        if (priority != null && !priority.isBlank() && !priority.equals(wo.getPriority())) return false;
        if (customerId != null && !customerId.equals(wo.getCustomer() != null ? wo.getCustomer().getId() : null)) return false;
        if (siteId != null && !siteId.equals(wo.getSite() != null ? wo.getSite().getId() : null)) return false;
        if (assigneeId != null && !assigneeId.equals(wo.getAssignee() != null ? wo.getAssignee().getId() : null)) return false;
        if (search != null && !search.isBlank()) {
            String s = search.toLowerCase();
            boolean matchesCode = wo.getCode() != null && wo.getCode().toLowerCase().contains(s);
            boolean matchesTitle = wo.getTitle() != null && wo.getTitle().toLowerCase().contains(s);
            if (!matchesCode && !matchesTitle) return false;
        }
        return true;
    }

    private OverdueWorkOrderResponse toOverdueResponse(WorkOrder wo, String type) {
        return new OverdueWorkOrderResponse(
                wo.getId(),
                wo.getCode(),
                wo.getTitle(),
                wo.getStatus(),
                wo.getDueDate() != null ? wo.getDueDate().toString() : null,
                wo.getSlaDueDate() != null ? wo.getSlaDueDate().toString() : null,
                type
        );
    }
}
