package com.keystone.backend.dto;

public class TimeLogResponse {

    private Long id;
    private Long workOrderId;
    private Long technicianId;
    private String technicianName;
    private Integer minutes;
    private String note;

    public TimeLogResponse() {
    }

    public TimeLogResponse(Long id, Long workOrderId, Long technicianId, String technicianName, Integer minutes, String note) {
        this.id = id;
        this.workOrderId = workOrderId;
        this.technicianId = technicianId;
        this.technicianName = technicianName;
        this.minutes = minutes;
        this.note = note;
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

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
