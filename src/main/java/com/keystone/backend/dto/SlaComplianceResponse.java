package com.keystone.backend.dto;

public class SlaComplianceResponse {

    private long totalWithSla;
    private long compliant;
    private long breached;
    private double complianceRate;

    public SlaComplianceResponse() {
    }

    public SlaComplianceResponse(long totalWithSla, long compliant, long breached, double complianceRate) {
        this.totalWithSla = totalWithSla;
        this.compliant = compliant;
        this.breached = breached;
        this.complianceRate = complianceRate;
    }

    public long getTotalWithSla() {
        return totalWithSla;
    }

    public void setTotalWithSla(long totalWithSla) {
        this.totalWithSla = totalWithSla;
    }

    public long getCompliant() {
        return compliant;
    }

    public void setCompliant(long compliant) {
        this.compliant = compliant;
    }

    public long getBreached() {
        return breached;
    }

    public void setBreached(long breached) {
        this.breached = breached;
    }

    public double getComplianceRate() {
        return complianceRate;
    }

    public void setComplianceRate(double complianceRate) {
        this.complianceRate = complianceRate;
    }
}
