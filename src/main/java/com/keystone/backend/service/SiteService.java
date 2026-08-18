package com.keystone.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keystone.backend.dto.SiteRequest;
import com.keystone.backend.dto.SiteResponse;
import com.keystone.backend.entity.Customer;
import com.keystone.backend.entity.Site;
import com.keystone.backend.exception.ResourceNotFoundException;
import com.keystone.backend.repository.CustomerRepository;
import com.keystone.backend.repository.SiteRepository;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final CustomerRepository customerRepository;

    public SiteService(SiteRepository siteRepository, CustomerRepository customerRepository) {
        this.siteRepository = siteRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<SiteResponse> getAllSites() {
        return siteRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SiteResponse> getSitesByCustomerId(Long customerId) {
        return siteRepository.findByCustomerId(customerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SiteResponse getSiteById(Long id) {
        return toResponse(findSiteOrThrow(id));
    }

    @Transactional
    public SiteResponse createSite(SiteRequest request) {
        Customer customer = findCustomerOrThrow(request.getCustomerId());

        Site site = new Site();
        site.setName(request.getName());
        site.setAddress(request.getAddress());
        site.setCustomer(customer);

        return toResponse(siteRepository.save(site));
    }

    @Transactional
    public SiteResponse updateSite(Long id, SiteRequest request) {
        Site existing = findSiteOrThrow(id);
        Customer customer = findCustomerOrThrow(request.getCustomerId());

        existing.setName(request.getName());
        existing.setAddress(request.getAddress());
        existing.setCustomer(customer);

        return toResponse(siteRepository.save(existing));
    }

    @Transactional
    public void deleteSite(Long id) {
        Site site = findSiteOrThrow(id);
        siteRepository.delete(site);
    }

    private Site findSiteOrThrow(Long id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + id));
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private SiteResponse toResponse(Site site) {
        Customer customer = site.getCustomer();
        return new SiteResponse(
                site.getId(),
                site.getName(),
                site.getAddress(),
                customer.getId(),
                customer.getName()
        );
    }
}
