package com.keystone.backend.dto;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private Long workOrderId;
    private String workOrderCode;
    private String type;
    private String message;
    private LocalDateTime createdAt;
    private boolean read;

    public NotificationResponse() {
    }

    public NotificationResponse(Long id, Long workOrderId, String workOrderCode, String type, String message,
            LocalDateTime createdAt, boolean read) {
        this.id = id;
        this.workOrderId = workOrderId;
        this.workOrderCode = workOrderCode;
        this.type = type;
        this.message = message;
        this.createdAt = createdAt;
        this.read = read;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getWorkOrderCode() {
        return workOrderCode;
    }

    public void setWorkOrderCode(String workOrderCode) {
        this.workOrderCode = workOrderCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
