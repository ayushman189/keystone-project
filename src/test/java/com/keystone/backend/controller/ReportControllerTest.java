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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.keystone.backend.entity.Customer;
import com.keystone.backend.entity.Site;
import com.keystone.backend.entity.User;
import com.keystone.backend.entity.WorkOrder;
import com.keystone.backend.security.JwtService;
import com.keystone.backend.repository.CustomerRepository;
import com.keystone.backend.repository.SiteRepository;
import com.keystone.backend.repository.UserRepository;
import com.keystone.backend.repository.WorkOrderRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

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
    private JwtService jwtService;

    private Customer customer;
    private Site site;
    private User manager;
    private User technician;
    private String managerToken;
    private String technicianToken;

    @BeforeEach
    void setUp() {
        workOrderRepository.deleteAll();
        siteRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();

        customer = customerRepository.save(new Customer("Report Test Customer"));
        site = siteRepository.save(new Site("Report Test Site", "456 Report St", customer));

        manager = new User();
        manager.setName("Manager Test");
        manager.setEmail("mgr@test.com");
        manager.setPassword("password");
        manager.setRole("MANAGER");
        manager = userRepository.save(manager);

        technician = new User();
        technician.setName("Tech Test");
        technician.setEmail("tech@test.com");
        technician.setPassword("password");
        technician.setRole("TECHNICIAN");
        technician = userRepository.save(technician);

        managerToken = jwtService.generateToken(manager.getEmail(), manager.getRole());
        technicianToken = jwtService.generateToken(technician.getEmail(), technician.getRole());
    }

    @Test
    void getSummary_asManager_shouldReturnDashboard() throws Exception {
        WorkOrder wo = new WorkOrder();
        wo.setCode("WO-DASH");
        wo.setTitle("Dashboard Test");
        wo.setPriority("high");
        wo.setStatus("Open");
        wo.setCustomer(customer);
        wo.setSite(site);
        workOrderRepository.save(wo);

        mockMvc.perform(get("/api/reports/summary")
                .header("Authorization", "Bearer " + managerToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWorkOrders", is(1)))
                .andExpect(jsonPath("$.statusCounts", hasSize(1)))
                .andExpect(jsonPath("$.statusCounts[0].status", is("Open")))
                .andExpect(jsonPath("$.statusCounts[0].count", is(1)))
                .andExpect(jsonPath("$.slaCompliance.totalWithSla", is(0)));
    }

    @Test
    void getSummary_asAdmin_shouldReturnDashboard() throws Exception {
        User admin = new User();
        admin.setName("Admin Test");
        admin.setEmail("admin@test.com");
        admin.setPassword("password");
        admin.setRole("ADMIN");
        admin = userRepository.save(admin);
        String adminToken = jwtService.generateToken(admin.getEmail(), admin.getRole());

        mockMvc.perform(get("/api/reports/summary")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWorkOrders", is(0)));
    }

    @Test
    void getSummary_asTechnician_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/reports/summary")
                .header("Authorization", "Bearer " + technicianToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSummary_withoutAuth_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/reports/summary")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSummary_statusCounts_shouldBeCorrect() throws Exception {
        WorkOrder wo1 = new WorkOrder();
        wo1.setCode("WO-1");
        wo1.setTitle("WO 1");
        wo1.setPriority("high");
        wo1.setStatus("Open");
        wo1.setCustomer(customer);
        wo1.setSite(site);
        workOrderRepository.save(wo1);

        WorkOrder wo2 = new WorkOrder();
        wo2.setCode("WO-2");
        wo2.setTitle("WO 2");
        wo2.setPriority("medium");
        wo2.setStatus("Open");
        wo2.setCustomer(customer);
        wo2.setSite(site);
        workOrderRepository.save(wo2);

        WorkOrder wo3 = new WorkOrder();
        wo3.setCode("WO-3");
        wo3.setTitle("WO 3");
        wo3.setPriority("low");
        wo3.setStatus("Done");
        wo3.setCustomer(customer);
        wo3.setSite(site);
        workOrderRepository.save(wo3);

        mockMvc.perform(get("/api/reports/summary")
                .header("Authorization", "Bearer " + managerToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWorkOrders", is(3)))
                .andExpect(jsonPath("$.statusCounts", hasSize(2)))
                .andExpect(jsonPath("$.statusCounts[0].status", is("Done")))
                .andExpect(jsonPath("$.statusCounts[0].count", is(1)))
                .andExpect(jsonPath("$.statusCounts[1].status", is("Open")))
                .andExpect(jsonPath("$.statusCounts[1].count", is(2)));
    }

    @Test
    void getSummary_slaCompliance_shouldBeCorrect() throws Exception {
        WorkOrder compliant = new WorkOrder();
        compliant.setCode("WO-COMP");
        compliant.setTitle("Compliant");
        compliant.setPriority("high");
        compliant.setStatus("Open");
        compliant.setSlaDueDate(java.time.LocalDateTime.now().plusDays(1));
        compliant.setCustomer(customer);
        compliant.setSite(site);
        workOrderRepository.save(compliant);

        WorkOrder breached = new WorkOrder();
        breached.setCode("WO-BREACH");
        breached.setTitle("Breached");
        breached.setPriority("medium");
        breached.setStatus("Open");
        breached.setSlaDueDate(java.time.LocalDateTime.now().minusHours(1));
        breached.setCustomer(customer);
        breached.setSite(site);
        workOrderRepository.save(breached);

        mockMvc.perform(get("/api/reports/summary")
                .header("Authorization", "Bearer " + managerToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slaCompliance.totalWithSla", is(2)))
                .andExpect(jsonPath("$.slaCompliance.compliant", is(1)))
                .andExpect(jsonPath("$.slaCompliance.breached", is(1)))
                .andExpect(jsonPath("$.slaCompliance.complianceRate", is(50.0)));
    }

    @Test
    void getSummary_overdueWorkOrders_shouldBeCorrect() throws Exception {
        WorkOrder overdueByDue = new WorkOrder();
        overdueByDue.setCode("WO-DUE");
        overdueByDue.setTitle("Due Overdue");
        overdueByDue.setPriority("high");
        overdueByDue.setStatus("Open");
        overdueByDue.setDueDate(java.time.LocalDateTime.now().minusDays(1));
        overdueByDue.setSlaDueDate(java.time.LocalDateTime.now().plusDays(1));
        overdueByDue.setCustomer(customer);
        overdueByDue.setSite(site);
        workOrderRepository.save(overdueByDue);

        WorkOrder overdueBySla = new WorkOrder();
        overdueBySla.setCode("WO-SLA");
        overdueBySla.setTitle("SLA Overdue");
        overdueBySla.setPriority("medium");
        overdueBySla.setStatus("Open");
        overdueBySla.setDueDate(java.time.LocalDateTime.now().plusDays(1));
        overdueBySla.setSlaDueDate(java.time.LocalDateTime.now().minusDays(1));
        overdueBySla.setCustomer(customer);
        overdueBySla.setSite(site);
        workOrderRepository.save(overdueBySla);

        mockMvc.perform(get("/api/reports/summary")
                .header("Authorization", "Bearer " + managerToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdueWorkOrders", hasSize(2)));
    }

    @Test
    void getSummary_technicianBreakdown_shouldBeCorrect() throws Exception {
        User tech1 = new User();
        tech1.setName("Alice");
        tech1.setEmail("alice@test.com");
        tech1.setPassword("password");
        tech1.setRole("TECHNICIAN");
        tech1 = userRepository.save(tech1);

        User tech2 = new User();
        tech2.setName("Bob");
        tech2.setEmail("bob@test.com");
        tech2.setPassword("password");
        tech2.setRole("TECHNICIAN");
        tech2 = userRepository.save(tech2);

        WorkOrder wo1 = new WorkOrder();
        wo1.setCode("WO-ALICE");
        wo1.setTitle("Alice Job");
        wo1.setPriority("high");
        wo1.setStatus("Open");
        wo1.setAssignee(tech1);
        wo1.setCustomer(customer);
        wo1.setSite(site);
        workOrderRepository.save(wo1);

        WorkOrder wo2 = new WorkOrder();
        wo2.setCode("WO-BOB");
        wo2.setTitle("Bob Job");
        wo2.setPriority("medium");
        wo2.setStatus("In Progress");
        wo2.setAssignee(tech2);
        wo2.setCustomer(customer);
        wo2.setSite(site);
        workOrderRepository.save(wo2);

        WorkOrder wo3 = new WorkOrder();
        wo3.setCode("WO-ALICE-2");
        wo3.setTitle("Alice Job 2");
        wo3.setPriority("low");
        wo3.setStatus("Done");
        wo3.setAssignee(tech1);
        wo3.setCustomer(customer);
        wo3.setSite(site);
        workOrderRepository.save(wo3);

        mockMvc.perform(get("/api/reports/summary")
                .header("Authorization", "Bearer " + managerToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technicianBreakdown", hasSize(2)))
                .andExpect(jsonPath("$.technicianBreakdown[0].status", is("Alice")))
                .andExpect(jsonPath("$.technicianBreakdown[0].count", is(2)))
                .andExpect(jsonPath("$.technicianBreakdown[1].status", is("Bob")))
                .andExpect(jsonPath("$.technicianBreakdown[1].count", is(1)));
    }

    @Test
    void getSummary_siteBreakdown_shouldBeCorrect() throws Exception {
        Site site2 = siteRepository.save(new Site("Site Two", "789 Site Ave", customer));

        WorkOrder wo1 = new WorkOrder();
        wo1.setCode("WO-SITE1");
        wo1.setTitle("Site 1 Job");
        wo1.setPriority("high");
        wo1.setStatus("Open");
        wo1.setSite(site);
        wo1.setCustomer(customer);
        workOrderRepository.save(wo1);

        WorkOrder wo2 = new WorkOrder();
        wo2.setCode("WO-SITE2");
        wo2.setTitle("Site 2 Job");
        wo2.setPriority("medium");
        wo2.setStatus("Open");
        wo2.setSite(site2);
        wo2.setCustomer(customer);
        workOrderRepository.save(wo2);

        WorkOrder wo3 = new WorkOrder();
        wo3.setCode("WO-SITE1-2");
        wo3.setTitle("Site 1 Job 2");
        wo3.setPriority("low");
        wo3.setStatus("Done");
        wo3.setSite(site);
        wo3.setCustomer(customer);
        workOrderRepository.save(wo3);

        mockMvc.perform(get("/api/reports/summary")
                .header("Authorization", "Bearer " + managerToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.siteBreakdown", hasSize(2)))
                .andExpect(jsonPath("$.siteBreakdown[0].status", is("Report Test Site")))
                .andExpect(jsonPath("$.siteBreakdown[0].count", is(2)))
                .andExpect(jsonPath("$.siteBreakdown[1].status", is("Site Two")))
                .andExpect(jsonPath("$.siteBreakdown[1].count", is(1)));
    }

    @Test
    void getSummary_withStatusFilter_shouldReturnFilteredResults() throws Exception {
        WorkOrder wo1 = new WorkOrder();
        wo1.setCode("WO-OPEN");
        wo1.setTitle("Open WO");
        wo1.setPriority("high");
        wo1.setStatus("Open");
        wo1.setCustomer(customer);
        wo1.setSite(site);
        workOrderRepository.save(wo1);

        WorkOrder wo2 = new WorkOrder();
        wo2.setCode("WO-DONE");
        wo2.setTitle("Done WO");
        wo2.setPriority("medium");
        wo2.setStatus("Done");
        wo2.setCustomer(customer);
        wo2.setSite(site);
        workOrderRepository.save(wo2);

        mockMvc.perform(get("/api/reports/summary")
                .header("Authorization", "Bearer " + managerToken)
                .param("status", "Open")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWorkOrders", is(1)))
                .andExpect(jsonPath("$.statusCounts[0].status", is("Open")))
                .andExpect(jsonPath("$.statusCounts[0].count", is(1)));
    }
}
