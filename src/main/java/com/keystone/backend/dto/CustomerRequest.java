package com.keystone.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerRequest {

    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Customer name must be at most 255 characters")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}