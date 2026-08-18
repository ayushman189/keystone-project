package com.keystone.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SiteRequest {

    @NotBlank(message = "Site name is required")
    @Size(max = 255, message = "Site name must be at most 255 characters")
    private String name;

    @NotBlank(message = "Site address is required")
    @Size(max = 255, message = "Site address must be at most 255 characters")
    private String address;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}