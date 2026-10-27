package com.fu.hotel.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fu.hotel.dto.request.AuthRequest;
import com.fu.hotel.dto.request.CustomerRegisterRequest;
import com.fu.hotel.dto.request.RoomRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAndControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Public endpoints should be accessible without authentication")
    void testPublicEndpointsAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/room-types"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected endpoints without token return 401 Unauthorized with JSON error")
    void testProtectedEndpointsWithoutTokenReturn401() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").isNotEmpty());

        mockMvc.perform(get("/api/customers/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Staff login with credentials from properties returns JWT with role STAFF")
    void testStaffLoginReturnsStaffToken() throws Exception {
        AuthRequest staffLogin = new AuthRequest("staff@fuminihotel.com", "Staff@123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(staffLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        String staffToken = jsonNode.get("token").asText();

        // Staff token can access customer management
        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Customer login returns JWT with role CUSTOMER and cannot access Staff endpoints (403 Forbidden)")
    void testCustomerTokenForbiddenOnStaffEndpoints() throws Exception {
        // customer1 was seeded in DataInitializer
        AuthRequest customerLogin = new AuthRequest("customer1@hotel.com", "Customer@123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        String customerToken = jsonNode.get("token").asText();

        // Customer can access /api/customers/me
        mockMvc.perform(get("/api/customers/me")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailAddress").value("customer1@hotel.com"));

        // Customer CANNOT access /api/customers (Staff only) -> 403 Forbidden
        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // Customer CANNOT create rooms -> 403 Forbidden
        RoomRequest roomRequest = new RoomRequest("999", "Desc", 2, 1, "1", new BigDecimal("100.00"));
        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Customer self-registration with valid data succeeds with 201 Created")
    void testCustomerRegistrationSuccess() throws Exception {
        CustomerRegisterRequest newCustomer = new CustomerRegisterRequest(
                "New User",
                "0933333333",
                "newuser@hotel.com",
                LocalDate.of(2000, 1, 1),
                "Password@123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emailAddress").value("newuser@hotel.com"))
                .andExpect(jsonPath("$.customerFullName").value("New User"));
    }

    @Test
    @DisplayName("Customer registration with invalid fields returns 400 Bad Request with field errors")
    void testCustomerRegistrationValidationErrors() throws Exception {
        CustomerRegisterRequest invalidCustomer = new CustomerRegisterRequest(
                "", // Blank
                "invalid-phone",
                "not-an-email",
                LocalDate.now().plusDays(1), // Future birthday
                "123" // Too short (< 6)
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCustomer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }
}
