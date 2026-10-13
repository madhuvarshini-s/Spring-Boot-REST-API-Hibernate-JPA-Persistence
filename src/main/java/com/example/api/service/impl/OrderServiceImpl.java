package com.example.api.service.impl;

import com.example.api.dto.request.OrderRequestDTO;
import com.example.api.dto.response.OrderResponseDTO;
import com.example.api.entity.Customer;
import com.example.api.entity.Order;
import com.example.api.exception.DuplicateResourceException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.repository.CustomerRepository;
import com.example.api.repository.OrderRepository;
import com.example.api.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        if (orderRepository.existsByOrderNumber(request.getOrderNumber())) {
            throw new DuplicateResourceException("An order with order number '" + request.getOrderNumber() + "' already exists.");
        }

        Order order = new Order(
                request.getOrderNumber(),
                request.getDescription(),
                request.getTotalAmount(),
                request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus() : "PENDING",
                LocalDateTime.now(),
                customer
        );

        customer.addOrder(order);
        Order saved = orderRepository.save(order);
        return mapToDTO(saved);
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToDTO(order);
    }

    @Override
    public List<OrderResponseDTO> getOrdersByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getOrdersAboveAmount(Double minAmount) {
        Double threshold = minAmount != null ? minAmount : 0.0;
        return orderRepository.findOrdersAboveAmount(threshold).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getOrdersByStatus(String status) {
        return orderRepository.findByStatusNative(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Double getTotalSpentByCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }
        return orderRepository.calculateTotalSpentByCustomer(customerId);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrder(Long id, OrderRequestDTO request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (orderRepository.existsByOrderNumberAndIdNot(request.getOrderNumber(), id)) {
            throw new DuplicateResourceException("An order with order number '" + request.getOrderNumber() + "' already exists.");
        }

        if (!order.getCustomer().getId().equals(request.getCustomerId())) {
            Customer newCustomer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
            order.setCustomer(newCustomer);
        }

        order.setOrderNumber(request.getOrderNumber());
        order.setDescription(request.getDescription());
        order.setTotalAmount(request.getTotalAmount());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            order.setStatus(request.getStatus());
        }

        Order updated = orderRepository.save(order);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        orderRepository.delete(order);
    }

    private OrderResponseDTO mapToDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getDescription(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderDate(),
                order.getCustomer() != null ? order.getCustomer().getId() : null,
                order.getCustomer() != null ? order.getCustomer().getName() : null
        );
    }
}
