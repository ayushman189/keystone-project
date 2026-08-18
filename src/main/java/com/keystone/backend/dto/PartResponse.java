package com.keystone.backend.dto;

import java.math.BigDecimal;

public class PartResponse {

    private Long id;
    private String name;
    private Integer stockQuantity;
    private BigDecimal unitCost;

    public PartResponse() {
    }

    public PartResponse(Long id, String name, Integer stockQuantity, BigDecimal unitCost) {
        this.id = id;
        this.name = name;
        this.stockQuantity = stockQuantity;
        this.unitCost = unitCost;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
