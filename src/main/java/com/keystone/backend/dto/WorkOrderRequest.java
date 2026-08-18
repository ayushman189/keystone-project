package com.keystone.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class WorkOrderRequest {

    @NotBlank(message = "Work order code is required")
    @Size(max = 255, message = "Work order code must be at most 255 characters")
    private String code;

    @NotBlank(message = "Work order title is required")
    @Size(max = 255, message = "Work order title must be at most 255 characters")
    private String title;

    private String description;

    @NotBlank(message = "Priority is required")
    @Size(max = 50, message = "Priority must be at most 50 characters")
    private String priority;

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must be at most 50 characters")
    private String status;

    private String slaDueDate;
    private String dueDate;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Site ID is required")
    private Long siteId;

    private Long assigneeId;

    public WorkOrderRequest() {
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

    public String getSlaDueDate() {
        return slaDueDate;
    }

    public void setSlaDueDate(String slaDueDate) {
        this.slaDueDate = slaDueDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}