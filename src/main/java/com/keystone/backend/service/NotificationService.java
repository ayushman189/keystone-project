package com.keystone.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keystone.backend.dto.NotificationResponse;
import com.keystone.backend.entity.Notification;
import com.keystone.backend.entity.WorkOrder;
import com.keystone.backend.exception.ResourceNotFoundException;
import com.keystone.backend.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public boolean existsForWorkOrder(WorkOrder workOrder, String type) {
        return notificationRepository.existsByWorkOrderAndType(workOrder, type);
    }

    @Transactional
    public NotificationResponse createSlaBreachNotification(WorkOrder workOrder) {
        if (existsForWorkOrder(workOrder, "SLA_BREACH")) {
            return null;
        }

        String message = "SLA breached for work order " + workOrder.getCode()
                + ": " + workOrder.getTitle()
                + ". Due date was " + formatDate(workOrder.getSlaDueDate()) + ".";

        Notification notification = new Notification();
        notification.setWorkOrder(workOrder);
        notification.setType("SLA_BREACH");
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);

        return new NotificationResponse(
                saved.getId(),
                workOrder.getId(),
                workOrder.getCode(),
                saved.getType(),
                saved.getMessage(),
                saved.getCreatedAt(),
                saved.isRead());
    }

    @Transactional(readOnly = true)
    public Iterable<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications() {
        return notificationRepository.findByReadFalse().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + id));
        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "unknown";
        }
        return dateTime.toString().replace("T", " ");
    }

    private NotificationResponse toResponse(Notification notification) {
        WorkOrder wo = notification.getWorkOrder();
        return new NotificationResponse(
                notification.getId(),
                wo != null ? wo.getId() : null,
                wo != null ? wo.getCode() : null,
                notification.getType(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.isRead());
    }
}
