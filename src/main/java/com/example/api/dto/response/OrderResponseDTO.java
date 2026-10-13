package com.example.api.dto.response;

import java.time.LocalDateTime;

public class OrderResponseDTO {

    private Long id;
    private String orderNumber;
    private String description;
    private Double totalAmount;
    private String status;
    private LocalDateTime orderDate;
    private Long customerId;
    private String customerName;

    public OrderResponseDTO() {
    }

    public OrderResponseDTO(Long id, String orderNumber, String description, Double totalAmount, String status, LocalDateTime orderDate, Long customerId, String customerName) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.description = description;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
        this.customerId = customerId;
        this.customerName = customerName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
