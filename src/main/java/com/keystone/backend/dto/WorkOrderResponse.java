package com.keystone.backend.dto;

import java.time.LocalDateTime;

public class WorkOrderResponse {

    private Long id;
    private String code;
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDateTime dueDate;
    private LocalDateTime slaDueDate;
    private Long customerId;
    private String customerName;
    private Long siteId;
    private String siteName;
    private Long assigneeId;
    private String assigneeName;

    public WorkOrderResponse() {
    }

    public WorkOrderResponse(
            Long id,
            String code,
            String title,
            String description,
            String priority,
            String status,
            LocalDateTime dueDate,
            LocalDateTime slaDueDate,
            Long customerId,
            String customerName,
            Long siteId,
            String siteName,
            Long assigneeId,
            String assigneeName) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.dueDate = dueDate;
        this.slaDueDate = slaDueDate;
        this.customerId = customerId;
        this.customerName = customerName;
        this.siteId = siteId;
        this.siteName = siteName;
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSlaDueDate() {
        return slaDueDate;
    }

    public void setSlaDueDate(LocalDateTime slaDueDate) {
        this.slaDueDate = slaDueDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }
}