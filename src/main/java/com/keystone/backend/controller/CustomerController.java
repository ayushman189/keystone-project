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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.keystone.backend.dto.CustomerRequest;
import com.keystone.backend.dto.CustomerResponse;
import com.keystone.backend.security.CurrentUser;
import com.keystone.backend.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
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
    public List<CustomerResponse> getAllCustomers() {
        requireRole("MANAGER", "ADMIN", "DISPATCHER");
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomerById(@PathVariable Long id) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER");
        return customerService.getCustomerById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER");
        return customerService.createCustomer(request);
    }

    @PutMapping("/{id}")
    public CustomerResponse updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        requireRole("MANAGER", "ADMIN", "DISPATCHER");
        return customerService.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id) {
        requireRole("MANAGER", "ADMIN");
        customerService.deleteCustomer(id);
    }
}
