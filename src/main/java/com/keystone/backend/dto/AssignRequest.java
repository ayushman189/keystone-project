package com.keystone.backend.dto;

import jakarta.validation.constraints.NotNull;

public class AssignRequest {

    @NotNull(message = "Technician ID is required")
    private Long technicianId;

    public AssignRequest() {
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }
}