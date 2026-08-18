package com.keystone.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.keystone.backend.dto.NotificationResponse;
import com.keystone.backend.security.CurrentUser;
import com.keystone.backend.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
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
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        requireRole("MANAGER", "ADMIN", "DISPATCHER", "TECHNICIAN");
        return ResponseEntity.ok((List<NotificationResponse>) notificationService.getAllNotifications());
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        requireRole("MANAGER", "ADMIN", "DISPATCHER", "TECHNICIAN");
        return ResponseEntity.ok(notificationService.getUnreadNotifications());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER", "TECHNICIAN");
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }
}
