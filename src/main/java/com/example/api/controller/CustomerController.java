package com.example.api.controller;

import com.example.api.dto.request.CustomerRequestDTO;
import com.example.api.dto.response.CustomerResponseDTO;
import com.example.api.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Create a new Customer with payload validation
     */
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO request) {
        CustomerResponseDTO created = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get all customers
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        List<CustomerResponseDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * Get single customer by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        CustomerResponseDTO customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Get customer details along with their associated orders (JOIN FETCH)
     */
    @GetMapping("/{id}/orders")
    public ResponseEntity<CustomerResponseDTO> getCustomerWithOrders(@PathVariable Long id) {
        CustomerResponseDTO customer = customerService.getCustomerWithOrders(id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Search customers by keyword in name or email (Custom JPQL Query)
     */
    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponseDTO>> searchCustomers(
            @RequestParam(name = "keyword", required = false) String keyword) {
        List<CustomerResponseDTO> results = customerService.searchCustomers(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * Update an existing Customer
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDTO request) {
        CustomerResponseDTO updated = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a customer by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
