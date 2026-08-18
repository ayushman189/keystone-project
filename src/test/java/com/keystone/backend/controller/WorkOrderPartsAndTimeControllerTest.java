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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.keystone.backend.entity.Customer;
import com.keystone.backend.entity.Site;
import com.keystone.backend.entity.User;
import com.keystone.backend.entity.WorkOrder;
import com.keystone.backend.entity.Part;
import com.keystone.backend.repository.CustomerRepository;
import com.keystone.backend.repository.SiteRepository;
import com.keystone.backend.repository.UserRepository;
import com.keystone.backend.repository.WorkOrderRepository;
import com.keystone.backend.repository.PartRepository;
import com.keystone.backend.repository.PartUsageRepository;
import com.keystone.backend.repository.TimeLogRepository;
import com.keystone.backend.repository.NotificationRepository;

@SpringBootTest
@AutoConfigureMockMvc
class WorkOrderPartsAndTimeControllerTest {

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
    private PartUsageRepository partUsageRepository;

    @Autowired
    private TimeLogRepository timeLogRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private Customer customer;
    private Site site;
    private User technician;
    private WorkOrder workOrder;
    private Part part;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        partUsageRepository.deleteAll();
        timeLogRepository.deleteAll();
        workOrderRepository.deleteAll();
        partRepository.deleteAll();
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

        workOrder = new WorkOrder();
        workOrder.setCode("WO-TEST");
        workOrder.setTitle("Test Work Order");
        workOrder.setPriority("high");
        workOrder.setStatus("Open");
        workOrder.setCustomer(customer);
        workOrder.setSite(site);
        workOrderRepository.save(workOrder);

        part = new Part("Air Filter", 10, new java.math.BigDecimal("12.50"));
        partRepository.save(part);
    }

    @Test
    void addPartUsage_shouldDecreaseStockAndReturnUsage() throws Exception {
        String body = """
            {
              "partId": %d,
              "quantity": 2
            }
            """.formatted(part.getId());

        mockMvc.perform(post("/api/work-orders/{id}/parts", workOrder.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partId", is(part.getId().intValue())))
                .andExpect(jsonPath("$.partName", is("Air Filter")))
                .andExpect(jsonPath("$.quantity", is(2)))
                .andExpect(jsonPath("$.unitCost", is(12.50)));
    }

    @Test
    void addPartUsage_insufficientStock_shouldReturnBadRequestAndNotDecreaseStock() throws Exception {
        String body = """
            {
              "partId": %d,
              "quantity": 20
            }
            """.formatted(part.getId());

        mockMvc.perform(post("/api/work-orders/{id}/parts", workOrder.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());

        var refreshedPart = partRepository.findById(part.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(10, refreshedPart.getStockQuantity());
    }

    @Test
    void getPartUsages_shouldReturnPartUsagesForWorkOrder() throws Exception {
        var usage = new com.keystone.backend.entity.PartUsage(workOrder, part, 2);
        partUsageRepository.save(usage);

        mockMvc.perform(get("/api/work-orders/{id}/parts", workOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].partName", is("Air Filter")))
                .andExpect(jsonPath("$[0].quantity", is(2)));
    }

    @Test
    void addTimeLog_shouldReturnTimeLog() throws Exception {
        String body = """
            {
              "technicianId": %d,
              "minutes": 45,
              "note": "Replaced capacitor"
            }
            """.formatted(technician.getId());

        mockMvc.perform(post("/api/work-orders/{id}/time-logs", workOrder.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.technicianId", is(technician.getId().intValue())))
                .andExpect(jsonPath("$.technicianName", is("Test Technician")))
                .andExpect(jsonPath("$.minutes", is(45)))
                .andExpect(jsonPath("$.note", is("Replaced capacitor")));
    }

    @Test
    void getTimeLogs_shouldReturnTimeLogsForWorkOrder() throws Exception {
        var timeLog = new com.keystone.backend.entity.TimeLog(workOrder, technician, 60, "General maintenance");
        timeLogRepository.save(timeLog);

        mockMvc.perform(get("/api/work-orders/{id}/time-logs", workOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].technicianName", is("Test Technician")))
                .andExpect(jsonPath("$[0].minutes", is(60)));
    }
}
