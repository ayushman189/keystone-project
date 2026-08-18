package com.keystone.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PartRequest {

    @NotBlank(message = "Part name is required")
    @Size(max = 255, message = "Part name must be at most 255 characters")
    private String name;

    @NotNull(message = "Stock quantity is required")
    private Integer stockQuantity;

    @NotNull(message = "Unit cost is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Unit cost must be >= 0")
    private BigDecimal unitCost;

    public PartRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
}
