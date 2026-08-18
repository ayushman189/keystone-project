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
import com.keystone.backend.entity.Site;
import com.keystone.backend.entity.User;
import com.keystone.backend.entity.WorkOrder;
import com.keystone.backend.repository.CustomerRepository;
import com.keystone.backend.repository.SiteRepository;
import com.keystone.backend.repository.UserRepository;
import com.keystone.backend.repository.WorkOrderRepository;
import com.keystone.backend.repository.NotificationRepository;

class WorkOrderControllerTest extends AuthenticatedControllerTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private Customer customer;
    private Site site;
    private User technician;
    private User dispatcher;
    private User manager;
    private String dispatcherToken;
    private String managerToken;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        workOrderRepository.deleteAll();
        siteRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
        customer = customerRepository.save(new Customer("Test Customer"));
        site = siteRepository.save(new Site("Test Site", "123 Test St", customer));
        technician = new User();
        technician.setName("Test Technician");
        technician.setEmail("tech@test.com");
        technician.setPassword("password");
        technician.setRole("TECHNICIAN");
        technician = userRepository.save(technician);
        dispatcher = createUser("disp-wo@test.com", "DISPATCHER");
        manager = createUser("mgr-wo@test.com", "MANAGER");
        dispatcherToken = tokenFor(dispatcher);
        managerToken = tokenFor(manager);
    }

    @Test
    void createWorkOrder_shouldReturnCreatedWorkOrder() throws Exception {
        String body = """
            {
              "code": "WO-001",
              "title": "Fix AC",
              "description": "AC not cooling",
              "priority": "high",
              "status": "Open",
              "customerId": %d,
              "siteId": %d
            }
            """.formatted(customer.getId(), site.getId());

        mockMvc.perform(post("/api/work-orders")
                .header("Authorization", "Bearer " + dispatcherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is("WO-001")))
                .andExpect(jsonPath("$.title", is("Fix AC")))
                .andExpect(jsonPath("$.priority", is("high")))
                .andExpect(jsonPath("$.status", is("Open")))
                .andExpect(jsonPath("$.customerId", is(customer.getId().intValue())))
                .andExpect(jsonPath("$.customerName", is("Test Customer")))
                .andExpect(jsonPath("$.siteId", is(site.getId().intValue())))
                .andExpect(jsonPath("$.siteName", is("Test Site")));
    }

    @Test
    void getAllWorkOrders_shouldReturnAllWorkOrders() throws Exception {
        WorkOrder wo1 = new WorkOrder();
        wo1.setCode("WO-001");
        wo1.setTitle("WO 1");
        wo1.setPriority("low");
        wo1.setStatus("Open");
        wo1.setCustomer(customer);
        wo1.setSite(site);
        workOrderRepository.save(wo1);

        WorkOrder wo2 = new WorkOrder();
        wo2.setCode("WO-002");
        wo2.setTitle("WO 2");
        wo2.setPriority("medium");
        wo2.setStatus("In Progress");
        wo2.setCustomer(customer);
        wo2.setSite(site);
        workOrderRepository.save(wo2);

        mockMvc.perform(get("/api/work-orders")
                .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getWorkOrderById_shouldReturnWorkOrder() throws Exception {
        WorkOrder wo = new WorkOrder();
        wo.setCode("WO-100");
        wo.setTitle("Repair");
        wo.setPriority("medium");
        wo.setStatus("In Progress");
        wo.setCustomer(customer);
        wo.setSite(site);
        wo = workOrderRepository.save(wo);

        mockMvc.perform(get("/api/work-orders/{id}", wo.getId())
                .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("WO-100")))
                .andExpect(jsonPath("$.customerName", is("Test Customer")))
                .andExpect(jsonPath("$.siteName", is("Test Site")));
    }

    @Test
    void updateWorkOrder_shouldReturnUpdatedWorkOrder() throws Exception {
        WorkOrder wo = new WorkOrder();
        wo.setCode("WO-200");
        wo.setTitle("Original");
        wo.setPriority("low");
        wo.setStatus("Open");
        wo.setCustomer(customer);
        wo.setSite(site);
        wo = workOrderRepository.save(wo);

        String body = """
            {
              "code": "WO-200",
              "title": "Updated Title",
              "description": "Updated description",
              "priority": "high",
              "status": "Done",
              "customerId": %d,
              "siteId": %d
            }
            """.formatted(customer.getId(), site.getId());

        mockMvc.perform(put("/api/work-orders/{id}", wo.getId())
                .header("Authorization", "Bearer " + dispatcherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.priority", is("high")))
                .andExpect(jsonPath("$.status", is("Done")));
    }

    @Test
    void deleteWorkOrder_shouldReturnNoContent() throws Exception {
        WorkOrder wo = new WorkOrder();
        wo.setCode("WO-300");
        wo.setTitle("Delete Me");
        wo.setPriority("low");
        wo.setStatus("Open");
        wo.setCustomer(customer);
        wo.setSite(site);
        wo = workOrderRepository.save(wo);

        mockMvc.perform(delete("/api/work-orders/{id}", wo.getId())
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/work-orders")
                .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createWorkOrder_withoutRequiredFields_shouldReturnBadRequest() throws Exception {
        String body = """
            {
              "code": "WO-400",
              "title": "Missing fields"
            }
            """;

        mockMvc.perform(post("/api/work-orders")
                .header("Authorization", "Bearer " + dispatcherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void assignTechnician_shouldAssignTechnicianToWorkOrder() throws Exception {
        WorkOrder wo = new WorkOrder();
        wo.setCode("WO-500");
        wo.setTitle("Assign Test");
        wo.setPriority("high");
        wo.setStatus("Open");
        wo.setCustomer(customer);
        wo.setSite(site);
        wo = workOrderRepository.save(wo);

        String body = """
            {
              "technicianId": %d
            }
            """.formatted(technician.getId());

        mockMvc.perform(put("/api/work-orders/{id}/assign", wo.getId())
                .header("Authorization", "Bearer " + dispatcherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId", is(technician.getId().intValue())))
                .andExpect(jsonPath("$.assigneeName", is("Test Technician")));
    }

    @Test
    void assignTechnician_workOrderNotFound_shouldReturnNotFound() throws Exception {
        String body = """
            {
              "technicianId": %d
            }
            """.formatted(technician.getId());

        mockMvc.perform(put("/api/work-orders/{id}/assign", 9999L)
                .header("Authorization", "Bearer " + dispatcherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignTechnician_technicianNotFound_shouldReturnNotFound() throws Exception {
        WorkOrder wo = new WorkOrder();
        wo.setCode("WO-501");
        wo.setTitle("Assign Test 2");
        wo.setPriority("high");
        wo.setStatus("Open");
        wo.setCustomer(customer);
        wo.setSite(site);
        wo = workOrderRepository.save(wo);

        String body = """
            {
              "technicianId": 9999
            }
            """;

        mockMvc.perform(put("/api/work-orders/{id}/assign", wo.getId())
                .header("Authorization", "Bearer " + dispatcherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignTechnician_nonTechnicianUser_shouldReturnBadRequest() throws Exception {
        User nonTech = new User();
        nonTech.setName("Regular User");
        nonTech.setEmail("regular@test.com");
        nonTech.setPassword("password");
        nonTech.setRole("CUSTOMER");
        nonTech = userRepository.save(nonTech);

        WorkOrder wo = new WorkOrder();
        wo.setCode("WO-502");
        wo.setTitle("Assign Test 3");
        wo.setPriority("high");
        wo.setStatus("Open");
        wo.setCustomer(customer);
        wo.setSite(site);
        wo = workOrderRepository.save(wo);

        String body = """
            {
              "technicianId": %d
            }
            """.formatted(nonTech.getId());

        mockMvc.perform(put("/api/work-orders/{id}/assign", wo.getId())
                .header("Authorization", "Bearer " + dispatcherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void assignTechnician_missingTechnicianId_shouldReturnBadRequest() throws Exception {
        WorkOrder wo = new WorkOrder();
        wo.setCode("WO-503");
        wo.setTitle("Assign Test 4");
        wo.setPriority("high");
        wo.setStatus("Open");
        wo.setCustomer(customer);
        wo.setSite(site);
        wo = workOrderRepository.save(wo);

        String body = """
            {
              "technicianId": null
            }
            """;

        mockMvc.perform(put("/api/work-orders/{id}/assign", wo.getId())
                .header("Authorization", "Bearer " + dispatcherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWorkOrdersByAssignee_shouldReturnWorkOrdersForAssignee() throws Exception {
        WorkOrder wo1 = new WorkOrder();
        wo1.setCode("WO-600");
        wo1.setTitle("WO Assigned");
        wo1.setPriority("high");
        wo1.setStatus("Open");
        wo1.setCustomer(customer);
        wo1.setSite(site);
        wo1.setAssignee(technician);
        workOrderRepository.save(wo1);

        WorkOrder wo2 = new WorkOrder();
        wo2.setCode("WO-601");
        wo2.setTitle("WO Unassigned");
        wo2.setPriority("low");
        wo2.setStatus("Open");
        wo2.setCustomer(customer);
        wo2.setSite(site);
        workOrderRepository.save(wo2);

        mockMvc.perform(get("/api/work-orders/assignee/{assigneeId}", technician.getId())
                .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is("WO-600")))
                .andExpect(jsonPath("$[0].assigneeId", is(technician.getId().intValue())));
    }

    @Test
    void getTechnicians_shouldReturnAllTechnicians() throws Exception {
        mockMvc.perform(get("/api/users/technicians")
                .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Technician")))
                .andExpect(jsonPath("$[0].role", is("TECHNICIAN")));
    }
}
