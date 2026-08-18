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

import com.keystone.backend.entity.User;
import com.keystone.backend.repository.PartRepository;

class PartControllerTest extends AuthenticatedControllerTest {

    @Autowired
    private PartRepository partRepository;

    private User manager;
    private String managerToken;

    @BeforeEach
    void setUp() {
        partRepository.deleteAll();
        userRepository.deleteAll();
        manager = createUser("mgr-parts@test.com", "MANAGER");
        managerToken = tokenFor(manager);
    }

    @Test
    void createPart_shouldReturnCreatedPart() throws Exception {
        String body = """
            {
              "name": "Air Filter",
              "stockQuantity": 50,
              "unitCost": 12.50
            }
            """;

        mockMvc.perform(post("/api/parts")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Air Filter")))
                .andExpect(jsonPath("$.stockQuantity", is(50)))
                .andExpect(jsonPath("$.unitCost", is(12.50)));
    }

    @Test
    void getAllParts_shouldReturnAllParts() throws Exception {
        var part1 = new com.keystone.backend.entity.Part("Air Filter", 50, new java.math.BigDecimal("12.50"));
        var part2 = new com.keystone.backend.entity.Part("Refrigerant", 30, new java.math.BigDecimal("85.00"));
        partRepository.save(part1);
        partRepository.save(part2);

        mockMvc.perform(get("/api/parts")
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getPartById_shouldReturnPart() throws Exception {
        var part = partRepository.save(new com.keystone.backend.entity.Part("Air Filter", 50, new java.math.BigDecimal("12.50")));

        mockMvc.perform(get("/api/parts/{id}", part.getId())
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Air Filter")))
                .andExpect(jsonPath("$.stockQuantity", is(50)));
    }

    @Test
    void updatePart_shouldReturnUpdatedPart() throws Exception {
        var part = partRepository.save(new com.keystone.backend.entity.Part("Air Filter", 50, new java.math.BigDecimal("12.50")));

        String body = """
            {
              "name": "Updated Filter",
              "stockQuantity": 60,
              "unitCost": 15.00
            }
            """;

        mockMvc.perform(put("/api/parts/{id}", part.getId())
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Filter")))
                .andExpect(jsonPath("$.stockQuantity", is(60)))
                .andExpect(jsonPath("$.unitCost", is(15.00)));
    }

    @Test
    void deletePart_shouldReturnNoContent() throws Exception {
        var part = partRepository.save(new com.keystone.backend.entity.Part("Air Filter", 50, new java.math.BigDecimal("12.50")));

        mockMvc.perform(delete("/api/parts/{id}", part.getId())
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/parts")
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createPart_withoutRequiredFields_shouldReturnBadRequest() throws Exception {
        String body = """
            {
              "name": ""
            }
            """;

        mockMvc.perform(post("/api/parts")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }
}
