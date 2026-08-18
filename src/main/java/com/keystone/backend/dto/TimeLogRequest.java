package com.keystone.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TimeLogRequest {

    @NotNull(message = "Technician ID is required")
    private Long technicianId;

    @NotNull(message = "Minutes is required")
    @Min(value = 1, message = "Minutes must be at least 1")
    private Integer minutes;

    private String note;

    public TimeLogRequest() {
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
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
