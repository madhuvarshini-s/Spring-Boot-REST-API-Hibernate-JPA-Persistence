package com.example.api.service;

import com.example.api.dto.request.OrderRequestDTO;
import com.example.api.dto.response.OrderResponseDTO;

import java.util.List;

public interface OrderService {

    OrderResponseDTO createOrder(OrderRequestDTO request);

    List<OrderResponseDTO> getAllOrders();

    OrderResponseDTO getOrderById(Long id);

    List<OrderResponseDTO> getOrdersByCustomerId(Long customerId);

    List<OrderResponseDTO> getOrdersAboveAmount(Double minAmount);

    List<OrderResponseDTO> getOrdersByStatus(String status);

    Double getTotalSpentByCustomer(Long customerId);

    OrderResponseDTO updateOrder(Long id, OrderRequestDTO request);

    void deleteOrder(Long id);
}
