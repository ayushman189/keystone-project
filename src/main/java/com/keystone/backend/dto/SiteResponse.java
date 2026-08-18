package com.keystone.backend.dto;

public class SiteResponse {

    private Long id;
    private String name;
    private String address;
    private Long customerId;
    private String customerName;

    public SiteResponse() {
    }

    public SiteResponse(Long id, String name, String address, Long customerId, String customerName) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.customerId = customerId;
        this.customerName = customerName;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}