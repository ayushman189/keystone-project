package com.keystone.backend.dto;

public class OverdueWorkOrderResponse {

    private Long id;
    private String code;
    private String title;
    private String status;
    private String dueDate;
    private String slaDueDate;
    private String overdueType;

    public OverdueWorkOrderResponse() {
    }

    public OverdueWorkOrderResponse(Long id, String code, String title, String status, String dueDate, String slaDueDate, String overdueType) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.status = status;
        this.dueDate = dueDate;
        this.slaDueDate = slaDueDate;
        this.overdueType = overdueType;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getSlaDueDate() {
        return slaDueDate;
    }

    public void setSlaDueDate(String slaDueDate) {
        this.slaDueDate = slaDueDate;
    }

    public String getOverdueType() {
        return overdueType;
    }

    public void setOverdueType(String overdueType) {
        this.overdueType = overdueType;
    }
}
