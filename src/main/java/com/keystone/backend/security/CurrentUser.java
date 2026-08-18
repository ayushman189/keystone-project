package com.keystone.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.keystone.backend.entity.User;

@Component
public class CurrentUser {

    public static User get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        return null;
    }

    public static Long getUserId() {
        User user = get();
        return user != null ? user.getId() : null;
    }

    public static String getRole() {
        User user = get();
        return user != null ? user.getRole() : null;
    }

    public static boolean hasRole(String... roles) {
        String currentRole = getRole();
        if (currentRole == null) {
            return false;
        }
        for (String role : roles) {
            if (currentRole.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
}
