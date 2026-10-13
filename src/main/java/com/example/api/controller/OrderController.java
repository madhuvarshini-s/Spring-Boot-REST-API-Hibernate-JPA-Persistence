package com.example.api.controller;

import com.example.api.dto.request.OrderRequestDTO;
import com.example.api.dto.response.OrderResponseDTO;
import com.example.api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Create a new Order for a customer with payload validation
     */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO request) {
        OrderResponseDTO created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get all orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Get single order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Get all orders belonging to a specific customer (Custom JPQL Query)
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByCustomerId(@PathVariable Long customerId) {
        List<OrderResponseDTO> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Filter orders with amount greater than or equal to threshold (Custom JPQL Query)
     */
    @GetMapping("/filter")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersAboveAmount(
            @RequestParam(name = "minAmount", defaultValue = "0.0") Double minAmount) {
        List<OrderResponseDTO> orders = orderService.getOrdersAboveAmount(minAmount);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by status (Custom Native Query)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(@PathVariable String status) {
        List<OrderResponseDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Calculate total spent by customer (Custom JPQL Aggregation)
     */
    @GetMapping("/customer/{customerId}/total-spent")
    public ResponseEntity<Map<String, Object>> getTotalSpentByCustomer(@PathVariable Long customerId) {
        Double total = orderService.getTotalSpentByCustomer(customerId);
        return ResponseEntity.ok(Map.of(
                "customerId", customerId,
                "totalSpent", total
        ));
    }

    /**
     * Update an existing Order
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequestDTO request) {
        OrderResponseDTO updated = orderService.updateOrder(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete an order by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
