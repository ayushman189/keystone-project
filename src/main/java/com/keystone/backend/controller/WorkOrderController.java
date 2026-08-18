package com.keystone.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.keystone.backend.dto.PartUsageRequest;
import com.keystone.backend.dto.PartUsageResponse;
import com.keystone.backend.dto.TimeLogRequest;
import com.keystone.backend.dto.TimeLogResponse;
import com.keystone.backend.dto.WorkOrderRequest;
import com.keystone.backend.dto.WorkOrderResponse;
import com.keystone.backend.security.CurrentUser;
import com.keystone.backend.service.WorkOrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    private void requireRole(String... roles) {
        String currentRole = CurrentUser.getRole();
        if (currentRole == null) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthenticated");
        }
        for (String role : roles) {
            if (role.equalsIgnoreCase(currentRole)) {
                return;
            }
        }
        throw new org.springframework.security.access.AccessDeniedException("Forbidden for role: " + currentRole);
    }

    @GetMapping
    public ResponseEntity<List<WorkOrderResponse>> getAllWorkOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "code") String sortBy) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER", "TECHNICIAN");
        return ResponseEntity.ok(workOrderService.getAllWorkOrders(status, priority, customerId, siteId, assigneeId, search));
    }

    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<List<WorkOrderResponse>> getWorkOrdersByAssignee(@PathVariable Long assigneeId) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER", "TECHNICIAN");
        return ResponseEntity.ok(workOrderService.getWorkOrdersByAssignee(assigneeId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<WorkOrderResponse>> getMyWorkOrders() {
        requireRole("CUSTOMER", "TECHNICIAN", "DISPATCHER", "MANAGER", "ADMIN");
        return ResponseEntity.ok(workOrderService.getMyWorkOrders());
    }

    @PutMapping("/{id}/assign")
    public WorkOrderResponse assignTechnician(
            @PathVariable Long id,
            @Valid @RequestBody com.keystone.backend.dto.AssignRequest request) {
        return workOrderService.assignTechnician(id, request.getTechnicianId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderResponse createWorkOrder(@Valid @RequestBody WorkOrderRequest request) {
        requireRole("DISPATCHER", "MANAGER", "ADMIN");
        return workOrderService.createWorkOrder(request);
    }

    @GetMapping("/{id}")
    public WorkOrderResponse getWorkOrderById(@PathVariable Long id) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER", "TECHNICIAN");
        return workOrderService.getWorkOrderById(id)
                .orElseThrow(() -> new com.keystone.backend.exception.ResourceNotFoundException(
                        "Work order not found with id: " + id));
    }

    @PutMapping("/{id}")
    public WorkOrderResponse updateWorkOrder(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderRequest request) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER", "TECHNICIAN");
        return workOrderService.updateWorkOrder(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkOrder(@PathVariable Long id) {
        requireRole("MANAGER", "ADMIN");
        workOrderService.deleteWorkOrder(id);
    }

    @PostMapping("/{id}/parts")
    @ResponseStatus(HttpStatus.CREATED)
    public PartUsageResponse addPartUsage(@PathVariable Long id, @Valid @RequestBody PartUsageRequest request) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER", "TECHNICIAN");
        return workOrderService.addPartUsage(id, request);
    }

    @GetMapping("/{id}/parts")
    public ResponseEntity<List<PartUsageResponse>> getPartUsages(@PathVariable Long id) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER", "TECHNICIAN");
        return ResponseEntity.ok(workOrderService.getPartUsagesByWorkOrder(id));
    }

    @PostMapping("/{id}/time-logs")
    @ResponseStatus(HttpStatus.CREATED)
    public TimeLogResponse addTimeLog(@PathVariable Long id, @Valid @RequestBody TimeLogRequest request) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER", "TECHNICIAN");
        return workOrderService.addTimeLog(id, request);
    }

    @GetMapping("/{id}/time-logs")
    public ResponseEntity<List<TimeLogResponse>> getTimeLogs(@PathVariable Long id) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER", "TECHNICIAN");
        return ResponseEntity.ok(workOrderService.getTimeLogsByWorkOrder(id));
    }
}