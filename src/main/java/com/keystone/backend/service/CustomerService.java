package com.keystone.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keystone.backend.dto.CustomerRequest;
import com.keystone.backend.dto.CustomerResponse;
import com.keystone.backend.entity.Customer;
import com.keystone.backend.exception.ResourceNotFoundException;
import com.keystone.backend.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        return toResponse(findCustomerOrThrow(id));
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer existing = findCustomerOrThrow(id);
        existing.setName(request.getName());
        return toResponse(customerRepository.save(existing));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = findCustomerOrThrow(id);
        customerRepository.delete(customer);
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getName());
    }
}