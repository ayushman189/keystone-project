package com.keystone.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keystone.backend.entity.WorkOrder;
import com.keystone.backend.repository.NotificationRepository;
import com.keystone.backend.repository.WorkOrderRepository;

@Service
public class SlaBreachChecker {

    private static final List<String> NON_BREACHABLE_STATUSES = List.of("Done", "Cancelled");

    private final WorkOrderRepository workOrderRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public SlaBreachChecker(WorkOrderRepository workOrderRepository,
            NotificationRepository notificationRepository,
            NotificationService notificationService) {
        this.workOrderRepository = workOrderRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRateString = "${sla.check.interval-ms:300000}")
    @Transactional
    public void checkSlaBreaches() {
        LocalDateTime now = LocalDateTime.now();
        List<WorkOrder> overdueWorkOrders = workOrderRepository.findBySlaDueDateBeforeAndStatusNotIn(now,
                NON_BREACHABLE_STATUSES);

        for (WorkOrder workOrder : overdueWorkOrders) {
            if (!notificationRepository.existsByWorkOrderAndType(workOrder, "SLA_BREACH")) {
                notificationService.createSlaBreachNotification(workOrder);
            }
        }
    }
}
