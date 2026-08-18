package com.keystone.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.keystone.backend.entity.Customer;
import com.keystone.backend.entity.Site;
import com.keystone.backend.entity.User;
import com.keystone.backend.entity.WorkOrder;
import com.keystone.backend.entity.Part;
import com.keystone.backend.service.SlaBreachChecker;
import com.keystone.backend.repository.CustomerRepository;
import com.keystone.backend.repository.SiteRepository;
import com.keystone.backend.repository.UserRepository;
import com.keystone.backend.repository.WorkOrderRepository;
import com.keystone.backend.repository.PartRepository;
import com.keystone.backend.repository.NotificationRepository;

@SpringBootTest
@AutoConfigureMockMvc
class SlaBreachControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SlaBreachChecker slaBreachChecker;

    private Customer customer;
    private Site site;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        workOrderRepository.deleteAll();
        partRepository.deleteAll();
        siteRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();

        customer = customerRepository.save(new Customer("Test Customer"));
        site = siteRepository.save(new Site("Test Site", "123 Test St", customer));
    }

    @Test
    void createWorkOrder_withSlaDueDate_shouldPersistAndReturn() throws Exception {
        String body = """
            {
              "code": "WO-SLA-001",
              "title": "SLA Test",
              "description": "Test SLA persistence",
              "priority": "high",
              "status": "Open",
              "slaDueDate": "2026-08-17T10:00:00",
              "customerId": %d,
              "siteId": %d
            }
            """.formatted(customer.getId(), site.getId());

        mockMvc.perform(post("/api/work-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slaDueDate", is("2026-08-17T10:00:00")));
    }

    @Test
    void slaBreach_detectsOverdueWorkOrder() throws Exception {
        WorkOrder overdue = new WorkOrder();
        overdue.setCode("WO-OVERDUE");
        overdue.setTitle("Overdue WO");
        overdue.setPriority("high");
        overdue.setStatus("Open");
        overdue.setSlaDueDate(java.time.LocalDateTime.now().minusHours(1));
        overdue.setCustomer(customer);
        overdue.setSite(site);
        workOrderRepository.save(overdue);

        slaBreachChecker.checkSlaBreaches();

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].workOrderCode", is("WO-OVERDUE")))
                .andExpect(jsonPath("$[0].type", is("SLA_BREACH")));
    }

    @Test
    void slaBreach_doesNotCreateDuplicateNotification() throws Exception {
        WorkOrder overdue = new WorkOrder();
        overdue.setCode("WO-DUP");
        overdue.setTitle("Duplicate Test");
        overdue.setPriority("high");
        overdue.setStatus("Open");
        overdue.setSlaDueDate(java.time.LocalDateTime.now().minusHours(2));
        overdue.setCustomer(customer);
        overdue.setSite(site);
        workOrderRepository.save(overdue);

        slaBreachChecker.checkSlaBreaches();
        slaBreachChecker.checkSlaBreaches();

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void slaBreach_doesNotNotifyCompletedWorkOrder() throws Exception {
        WorkOrder done = new WorkOrder();
        done.setCode("WO-DONE");
        done.setTitle("Done WO");
        done.setPriority("high");
        done.setStatus("Done");
        done.setSlaDueDate(java.time.LocalDateTime.now().minusHours(3));
        done.setCustomer(customer);
        done.setSite(site);
        workOrderRepository.save(done);

        slaBreachChecker.checkSlaBreaches();

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void markNotificationAsRead_shouldUpdate() throws Exception {
        WorkOrder overdue = new WorkOrder();
        overdue.setCode("WO-READ");
        overdue.setTitle("Read Test");
        overdue.setPriority("high");
        overdue.setStatus("Open");
        overdue.setSlaDueDate(java.time.LocalDateTime.now().minusHours(4));
        overdue.setCustomer(customer);
        overdue.setSite(site);
        workOrderRepository.save(overdue);

        slaBreachChecker.checkSlaBreaches();

        var notifications = notificationRepository.findAll();
        Long notificationId = notifications.iterator().next().getId();

        mockMvc.perform(put("/api/notifications/{id}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read", is(true)));
    }
}
