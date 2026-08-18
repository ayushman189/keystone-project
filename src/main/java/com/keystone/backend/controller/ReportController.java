package com.keystone.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.keystone.backend.dto.DashboardSummaryResponse;
import com.keystone.backend.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    @ResponseStatus(HttpStatus.OK)
    public DashboardSummaryResponse getSummary(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String search) {
        return reportService.getDashboardSummary(status, priority, customerId, siteId, assigneeId, search);
    }
}
