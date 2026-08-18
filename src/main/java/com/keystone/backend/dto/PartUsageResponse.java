package com.keystone.backend.dto;

import java.math.BigDecimal;

public class PartUsageResponse {

    private Long id;
    private Long workOrderId;
    private Long partId;
    private String partName;
    private Integer quantity;
    private BigDecimal unitCost;

    public PartUsageResponse() {
    }

    public PartUsageResponse(Long id, Long workOrderId, Long partId, String partName, Integer quantity, BigDecimal unitCost) {
        this.id = id;
        this.workOrderId = workOrderId;
        this.partId = partId;
        this.partName = partName;
        this.quantity = quantity;
        this.unitCost = unitCost;
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

    public Long getPartId() {
        return partId;
    }

    public void setPartId(Long partId) {
        this.partId = partId;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
}
