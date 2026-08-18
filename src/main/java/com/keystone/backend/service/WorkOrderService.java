package com.keystone.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keystone.backend.dto.PartUsageRequest;
import com.keystone.backend.dto.PartUsageResponse;
import com.keystone.backend.dto.TimeLogRequest;
import com.keystone.backend.dto.TimeLogResponse;
import com.keystone.backend.dto.WorkOrderRequest;
import com.keystone.backend.dto.WorkOrderResponse;
import com.keystone.backend.entity.Customer;
import com.keystone.backend.entity.Part;
import com.keystone.backend.entity.PartUsage;
import com.keystone.backend.entity.Site;
import com.keystone.backend.entity.TimeLog;
import com.keystone.backend.entity.User;
import com.keystone.backend.entity.WorkOrder;
import com.keystone.backend.entity.WorkOrderStatusHistory;
import com.keystone.backend.exception.InsufficientStockException;
import com.keystone.backend.exception.ResourceNotFoundException;
import com.keystone.backend.repository.CustomerRepository;
import com.keystone.backend.repository.PartRepository;
import com.keystone.backend.repository.PartUsageRepository;
import com.keystone.backend.repository.SiteRepository;
import com.keystone.backend.repository.TimeLogRepository;
import com.keystone.backend.repository.UserRepository;
import com.keystone.backend.repository.WorkOrderRepository;
import com.keystone.backend.repository.WorkOrderStatusHistoryRepository;
import com.keystone.backend.security.CurrentUser;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final CustomerRepository customerRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final WorkOrderStatusHistoryRepository statusHistoryRepository;
    private final PartRepository partRepository;
    private final PartUsageRepository partUsageRepository;
    private final TimeLogRepository timeLogRepository;

    public WorkOrderService(
            WorkOrderRepository workOrderRepository,
            CustomerRepository customerRepository,
            SiteRepository siteRepository,
            UserRepository userRepository,
            WorkOrderStatusHistoryRepository statusHistoryRepository,
            PartRepository partRepository,
            PartUsageRepository partUsageRepository,
            TimeLogRepository timeLogRepository) {
        this.workOrderRepository = workOrderRepository;
        this.customerRepository = customerRepository;
        this.siteRepository = siteRepository;
        this.userRepository = userRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.partRepository = partRepository;
        this.partUsageRepository = partUsageRepository;
        this.timeLogRepository = timeLogRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> getAllWorkOrders(String status, String priority, Long customerId, Long siteId, Long assigneeId, String search) {
        return workOrderRepository.findByFilter(status, priority, customerId, siteId, assigneeId, search).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> getWorkOrdersByAssignee(Long assigneeId) {
        return workOrderRepository.findByFilter(null, null, null, null, assigneeId, null).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> getMyWorkOrders() {
        com.keystone.backend.entity.User currentUser = CurrentUser.get();
        if (currentUser == null) {
            throw new RuntimeException("Unauthenticated");
        }

        String role = currentUser.getRole();
        if ("CUSTOMER".equals(role)) {
            if (currentUser.getCustomer() == null) {
                return List.of();
            }
            return workOrderRepository.findByFilter(null, null, currentUser.getCustomer().getId(), null, null, null).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } else if ("TECHNICIAN".equals(role)) {
            return workOrderRepository.findByFilter(null, null, null, null, currentUser.getId(), null).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return getAllWorkOrders(null, null, null, null, null, null);
    }

    @Transactional
    public WorkOrderResponse assignTechnician(Long workOrderId, Long technicianId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work order not found with id: " + workOrderId));
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + technicianId));
        if (!"TECHNICIAN".equals(technician.getRole())) {
            throw new IllegalArgumentException(
                    "User with id " + technicianId + " is not a technician");
        }
        String oldStatus = workOrder.getStatus();
        workOrder.setAssignee(technician);
        workOrderRepository.save(workOrder);

        // Record status history for the assignment
        WorkOrderStatusHistory history = new WorkOrderStatusHistory();
        history.setWorkOrder(workOrder);
        history.setFromStatus(oldStatus);
        history.setToStatus(oldStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setNote("Technician " + technician.getName() + " assigned");
        statusHistoryRepository.save(history);

        return toResponse(workOrder);
    }

    @Transactional(readOnly = true)
    public Optional<WorkOrderResponse> getWorkOrderById(Long id) {
        return workOrderRepository.findById(id).map(this::toResponse);
    }

    @Transactional
    public WorkOrderResponse createWorkOrder(WorkOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + request.getCustomerId()));
        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Site not found with id: " + request.getSiteId()));
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + request.getAssigneeId()));
        }

        WorkOrder workOrder = new WorkOrder();
        workOrder.setCode(request.getCode());
        workOrder.setTitle(request.getTitle());
        workOrder.setDescription(request.getDescription());
        workOrder.setPriority(request.getPriority());
        // Set initial status to Open (default for new work orders)
        String initialStatus = WorkOrderStateMachine.getInitialStatus();
        workOrder.setStatus(initialStatus);
        workOrder.setDueDate(parseDate(request.getDueDate()));
        workOrder.setSlaDueDate(parseDate(request.getSlaDueDate()));
        workOrder.setCustomer(customer);
        workOrder.setSite(site);
        workOrder.setAssignee(assignee);

        // Save work order first to get an ID
        workOrderRepository.save(workOrder);

        // Create initial status history entry after work order is saved
        WorkOrderStatusHistory history = new WorkOrderStatusHistory();
        history.setWorkOrder(workOrder);
        history.setFromStatus(null);
        history.setToStatus(initialStatus);
        history.setChangedAt(LocalDateTime.now());
        // Changed by is optional for creation
        history.setNote("Work order created");
        statusHistoryRepository.save(history);

        return toResponse(workOrder);
    }

    @Transactional
    public WorkOrderResponse updateWorkOrder(Long id, WorkOrderRequest request) {
        WorkOrder existing = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work order not found with id: " + id));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + request.getCustomerId()));
        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Site not found with id: " + request.getSiteId()));
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + request.getAssigneeId()));
        }

        String newStatus = request.getStatus();
        
        // Validate status transition using state machine
        if (newStatus != null && !existing.getStatus().equals(newStatus)) {
            if (!WorkOrderStateMachine.isValidTransition(existing.getStatus(), newStatus)) {
                throw new IllegalArgumentException(
                        "Invalid status transition from '" + existing.getStatus() + 
                        "' to '" + newStatus + "'");
            }
            String oldStatus = existing.getStatus();
            existing.setStatus(newStatus);
            
            // Create status history entry
            WorkOrderStatusHistory history = new WorkOrderStatusHistory();
            history.setWorkOrder(existing);
            history.setFromStatus(oldStatus);
            history.setToStatus(newStatus);
            history.setChangedAt(LocalDateTime.now());
            // No user ID provided in this method signature
            history.setNote("Status changed to " + newStatus);
            statusHistoryRepository.save(history);
        } else if (newStatus != null && existing.getStatus().equals(newStatus)) {
            // Same status - still record as a history entry for audit purposes
            WorkOrderStatusHistory history = new WorkOrderStatusHistory();
            history.setWorkOrder(existing);
            history.setFromStatus(existing.getStatus());
            history.setToStatus(newStatus);
            history.setChangedAt(LocalDateTime.now());
            history.setNote("Status unchanged: " + newStatus);
            statusHistoryRepository.save(history);
        }

        existing.setCode(request.getCode());
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setPriority(request.getPriority());
        existing.setDueDate(parseDate(request.getDueDate()));
        existing.setSlaDueDate(parseDate(request.getSlaDueDate()));
        existing.setCustomer(customer);
        existing.setSite(site);
        existing.setAssignee(assignee);

        return toResponse(workOrderRepository.save(existing));
    }

    @Transactional
    public WorkOrder saveWorkOrder(WorkOrder workOrder) {
        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public void deleteWorkOrder(Long id) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work order not found with id: " + id));
        workOrderRepository.delete(workOrder);
    }

    @Transactional
    public PartUsageResponse addPartUsage(Long workOrderId, PartUsageRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work order not found with id: " + workOrderId));
        Part part = partRepository.findById(request.getPartId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Part not found with id: " + request.getPartId()));

        Integer newStock = part.getStockQuantity() - request.getQuantity();
        if (newStock < 0) {
            throw new InsufficientStockException(
                    "Insufficient stock for part " + part.getName() + 
                    ". Available: " + part.getStockQuantity() + 
                    ", Requested: " + request.getQuantity());
        }

        part.setStockQuantity(newStock);
        partRepository.save(part);

        PartUsage usage = new PartUsage();
        usage.setWorkOrder(workOrder);
        usage.setPart(part);
        usage.setQuantity(request.getQuantity());
        PartUsage saved = partUsageRepository.save(usage);

        return new PartUsageResponse(
                saved.getId(),
                workOrder.getId(),
                part.getId(),
                part.getName(),
                saved.getQuantity(),
                part.getUnitCost()
        );
    }

    @Transactional(readOnly = true)
    public List<PartUsageResponse> getPartUsagesByWorkOrder(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work order not found with id: " + workOrderId));
        return partUsageRepository.findByWorkOrder(workOrder).stream()
                .map(usage -> new PartUsageResponse(
                        usage.getId(),
                        workOrder.getId(),
                        usage.getPart().getId(),
                        usage.getPart().getName(),
                        usage.getQuantity(),
                        usage.getPart().getUnitCost()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public TimeLogResponse addTimeLog(Long workOrderId, TimeLogRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work order not found with id: " + workOrderId));
        User technician = userRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Technician not found with id: " + request.getTechnicianId()));
        if (!"TECHNICIAN".equals(technician.getRole())) {
            throw new IllegalArgumentException(
                    "User with id " + request.getTechnicianId() + " is not a technician");
        }

        TimeLog timeLog = new TimeLog();
        timeLog.setWorkOrder(workOrder);
        timeLog.setTechnician(technician);
        timeLog.setMinutes(request.getMinutes());
        timeLog.setNote(request.getNote());
        TimeLog saved = timeLogRepository.save(timeLog);

        return new TimeLogResponse(
                saved.getId(),
                workOrder.getId(),
                technician.getId(),
                technician.getName(),
                saved.getMinutes(),
                saved.getNote()
        );
    }

    @Transactional(readOnly = true)
    public List<TimeLogResponse> getTimeLogsByWorkOrder(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work order not found with id: " + workOrderId));
        return timeLogRepository.findByWorkOrder(workOrder).stream()
                .map(log -> new TimeLogResponse(
                        log.getId(),
                        workOrder.getId(),
                        log.getTechnician().getId(),
                        log.getTechnician().getName(),
                        log.getMinutes(),
                        log.getNote()
                ))
                .collect(Collectors.toList());
    }

    private LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            LocalDate date = LocalDate.parse(value);
            return date.atStartOfDay();
        }
    }

    private WorkOrderResponse toResponse(WorkOrder workOrder) {
        Customer customer = workOrder.getCustomer();
        Site site = workOrder.getSite();
        User assignee = workOrder.getAssignee();

        return new WorkOrderResponse(
                workOrder.getId(),
                workOrder.getCode(),
                workOrder.getTitle(),
                workOrder.getDescription(),
                workOrder.getPriority(),
                workOrder.getStatus(),
                workOrder.getDueDate(),
                workOrder.getSlaDueDate(),
                customer != null ? customer.getId() : null,
                customer != null ? customer.getName() : null,
                site != null ? site.getId() : null,
                site != null ? site.getName() : null,
                assignee != null ? assignee.getId() : null,
                assignee != null ? assignee.getName() : null
        );
    }
}