package com.example.api;

import com.example.api.dto.request.CustomerRequestDTO;
import com.example.api.dto.request.OrderRequestDTO;
import com.example.api.repository.CustomerRepository;
import com.example.api.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
public class CustomerOrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/customers - Should create customer when payload is valid")
    void testCreateCustomerSuccess() throws Exception {
        CustomerRequestDTO request = new CustomerRequestDTO(
                "Alice Smith",
                "alice.smith@example.com",
                "+1-555-0199",
                "123 Tech Boulevard"
        );

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name", is("Alice Smith")))
                .andExpect(jsonPath("$.email", is("alice.smith@example.com")))
                .andExpect(jsonPath("$.phone", is("+1-555-0199")))
                .andExpect(jsonPath("$.address", is("123 Tech Boulevard")));
    }

    @Test
    @DisplayName("POST /api/v1/customers - Should return 400 when Jakarta validation fails")
    void testCreateCustomerValidationError() throws Exception {
        CustomerRequestDTO invalidRequest = new CustomerRequestDTO(
                "", // Blank name - violates @NotBlank and @Size
                "invalid-email-address", // Invalid email - violates @Email
                null,
                null
        );

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("POST /api/v1/customers - Should return 409 Conflict when email already exists")
    void testCreateCustomerDuplicateEmail() throws Exception {
        CustomerRequestDTO request = new CustomerRequestDTO(
                "Bob Johnson",
                "bob.johnson@example.com",
                "+1-555-0200",
                "456 Market St"
        );

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Attempting to create duplicate customer with same email
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Duplicate Resource Conflict")));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} - Should return 404 Not Found for non-existing ID")
    void testGetCustomerNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/customers/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Resource Not Found")));
    }

    @Test
    @DisplayName("Order CRUD & Custom JPQL Queries integration test")
    void testOrderCrudAndCustomQueries() throws Exception {
        // 1. Create a customer
        CustomerRequestDTO custRequest = new CustomerRequestDTO(
                "Charlie Brown",
                "charlie.brown@example.com",
                "+1-555-0300",
                "789 Oak Lane"
        );

        String custResponseJson = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(custRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long customerId = objectMapper.readTree(custResponseJson).get("id").asLong();

        // 2. Create Order 1 for Charlie
        OrderRequestDTO order1 = new OrderRequestDTO(
                customerId,
                "ORD-1001",
                "High-end Laptop",
                1299.99,
                "COMPLETED"
        );

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber", is("ORD-1001")))
                .andExpect(jsonPath("$.totalAmount", is(1299.99)))
                .andExpect(jsonPath("$.customerId", is(customerId.intValue())))
                .andExpect(jsonPath("$.customerName", is("Charlie Brown")));

        // 3. Create Order 2 for Charlie
        OrderRequestDTO order2 = new OrderRequestDTO(
                customerId,
                "ORD-1002",
                "Wireless Mechanical Keyboard",
                149.50,
                "PENDING"
        );

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order2)))
                .andExpect(status().isCreated());

        // 4. Test Custom Query: find orders by customer ID
        mockMvc.perform(get("/api/v1/orders/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // 5. Test Custom Query with JOIN FETCH: Customer with Orders
        mockMvc.perform(get("/api/v1/customers/{id}/orders", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customerId.intValue())))
                .andExpect(jsonPath("$.orders", hasSize(2)))
                .andExpect(jsonPath("$.totalOrders", is(2)));

        // 6. Test Custom JPQL Query: filter orders above minimum amount
        mockMvc.perform(get("/api/v1/orders/filter").param("minAmount", "500.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderNumber", is("ORD-1001")));

        // 7. Test Custom JPQL Aggregation: calculate total spent
        mockMvc.perform(get("/api/v1/orders/customer/{customerId}/total-spent", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId", is(customerId.intValue())))
                .andExpect(jsonPath("$.totalSpent", closeTo(1449.49, 0.01)));

        // 8. Test Custom Native Query: filter orders by status
        mockMvc.perform(get("/api/v1/orders/status/{status}", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderNumber", is("ORD-1001")));

        // 9. Test PUT update order
        OrderRequestDTO updateOrderReq = new OrderRequestDTO(
                customerId,
                "ORD-1001",
                "High-end Laptop (Upgraded RAM)",
                1499.99,
                "SHIPPED"
        );

        Long order1Id = orderRepository.findByOrderNumber("ORD-1001").orElseThrow().getId();
        mockMvc.perform(put("/api/v1/orders/{id}", order1Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateOrderReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("High-end Laptop (Upgraded RAM)")))
                .andExpect(jsonPath("$.totalAmount", is(1499.99)))
                .andExpect(jsonPath("$.status", is("SHIPPED")));

        // 10. Test DELETE order
        mockMvc.perform(delete("/api/v1/orders/{id}", order1Id))
                .andExpect(status().isNoContent());

        // Verify order is deleted
        mockMvc.perform(get("/api/v1/orders/{id}", order1Id))
                .andExpect(status().isNotFound());
    }
}
