package com.keystone.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.keystone.backend.dto.SiteRequest;
import com.keystone.backend.dto.SiteResponse;
import com.keystone.backend.security.CurrentUser;
import com.keystone.backend.service.SiteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    private void requireRole(String... roles) {
        String currentRole = CurrentUser.getRole();
        if (currentRole == null) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthenticated");
        }
        for (String role : roles) {
            if (role.equalsIgnoreCase(currentRole)) {
                return;
            }
        }
        throw new org.springframework.security.access.AccessDeniedException("Forbidden for role: " + currentRole);
    }

    @GetMapping
    public List<SiteResponse> getAllSites(
            @RequestParam(required = false) Long customerId) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER");
        if (customerId != null) {
            return siteService.getSitesByCustomerId(customerId);
        }
        return siteService.getAllSites();
    }

    @GetMapping("/{id}")
    public SiteResponse getSiteById(@PathVariable Long id) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER");
        return siteService.getSiteById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SiteResponse createSite(@Valid @RequestBody SiteRequest request) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER");
        return siteService.createSite(request);
    }

    @PutMapping("/{id}")
    public SiteResponse updateSite(
            @PathVariable Long id,
            @Valid @RequestBody SiteRequest request) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER");
        return siteService.updateSite(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSite(@PathVariable Long id) {
        requireRole("MANAGER", "ADMIN");
        siteService.deleteSite(id);
    }
}