package com.keystone.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.keystone.backend.entity.Customer;
import com.keystone.backend.entity.User;
import com.keystone.backend.repository.CustomerRepository;
import com.keystone.backend.repository.SiteRepository;

class SiteControllerTest extends AuthenticatedControllerTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SiteRepository siteRepository;

    private Customer customer;
    private User dispatcher;
    private String dispatcherToken;

    @BeforeEach
    void setUp() {
        siteRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
        customer = customerRepository.save(new Customer("Test Customer"));
        dispatcher = createUser("disp-sites@test.com", "DISPATCHER");
        dispatcherToken = tokenFor(dispatcher);
    }

    @Test
    void createSite_shouldReturnCreatedSite() throws Exception {
        String body = """
            {
              "name": "Main Office",
              "address": "123 Main St",
              "customerId": %d
            }
            """.formatted(customer.getId());

        mockMvc.perform(post("/api/sites")
                .header("Authorization", "Bearer " + dispatcherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Main Office")))
                .andExpect(jsonPath("$.address", is("123 Main St")))
                .andExpect(jsonPath("$.customerId", is(customer.getId().intValue())))
                .andExpect(jsonPath("$.customerName", is("Test Customer")));
    }

    @Test
    void getAllSites_shouldReturnAllSites() throws Exception {
        siteRepository.save(new com.keystone.backend.entity.Site("Site A", "Addr A", customer));
        siteRepository.save(new com.keystone.backend.entity.Site("Site B", "Addr B", customer));

        mockMvc.perform(get("/api/sites")
                .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getSitesByCustomerId_shouldFilterByCustomer() throws Exception {
        siteRepository.save(new com.keystone.backend.entity.Site("Site A", "Addr A", customer));

        mockMvc.perform(get("/api/sites")
                .header("Authorization", "Bearer " + dispatcherToken)
                .param("customerId", customer.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Site A")));
    }

    @Test
    void getSiteById_shouldReturnSite() throws Exception {
        var site = siteRepository.save(new com.keystone.backend.entity.Site("Site A", "Addr A", customer));

        mockMvc.perform(get("/api/sites/{id}", site.getId())
                .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Site A")))
                .andExpect(jsonPath("$.customerName", is("Test Customer")));
    }

    @Test
    void updateSite_shouldReturnUpdatedSite() throws Exception {
        var site = siteRepository.save(new com.keystone.backend.entity.Site("Site A", "Addr A", customer));

        String body = """
            {
              "name": "Updated Site",
              "address": "456 New St",
              "customerId": %d
            }
            """.formatted(customer.getId());

        mockMvc.perform(put("/api/sites/{id}", site.getId())
                .header("Authorization", "Bearer " + dispatcherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Site")))
                .andExpect(jsonPath("$.address", is("456 New St")));
    }

    @Test
    void deleteSite_shouldReturnNoContent() throws Exception {
        var site = siteRepository.save(new com.keystone.backend.entity.Site("Site A", "Addr A", customer));

        mockMvc.perform(delete("/api/sites/{id}", site.getId())
                .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/sites")
                .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createSite_withoutCustomer_shouldReturnBadRequest() throws Exception {
        String body = """
            {
              "name": "No Customer Site",
              "address": "789 Nowhere"
            }
            """;

        mockMvc.perform(post("/api/sites")
                .header("Authorization", "Bearer " + dispatcherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }
}
