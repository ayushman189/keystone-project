package com.keystone.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.keystone.backend.dto.PartRequest;
import com.keystone.backend.dto.PartResponse;
import com.keystone.backend.security.CurrentUser;
import com.keystone.backend.service.PartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/parts")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
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
    public ResponseEntity<List<PartResponse>> getAllParts() {
        requireRole("MANAGER", "ADMIN");
        return ResponseEntity.ok(partService.getAllParts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartResponse> getPartById(@PathVariable Long id) {
        requireRole("MANAGER", "ADMIN");
        return ResponseEntity.ok(partService.getPartById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartResponse createPart(@Valid @RequestBody PartRequest request) {
        requireRole("MANAGER", "ADMIN");
        return partService.createPart(request);
    }

    @PutMapping("/{id}")
    public PartResponse updatePart(@PathVariable Long id, @Valid @RequestBody PartRequest request) {
        requireRole("MANAGER", "ADMIN");
        return partService.updatePart(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePart(@PathVariable Long id) {
        requireRole("MANAGER", "ADMIN");
        partService.deletePart(id);
    }
}
