package com.keystone.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.keystone.backend.dto.UserResponse;
import com.keystone.backend.entity.User;
import com.keystone.backend.repository.UserRepository;
import com.keystone.backend.security.CurrentUser;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    @GetMapping("/technicians")
    public ResponseEntity<List<UserResponse>> getTechnicians() {
        requireRole("MANAGER", "ADMIN", "DISPATCHER");
        List<UserResponse> technicians = userRepository.findByRole("TECHNICIAN").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(technicians);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}