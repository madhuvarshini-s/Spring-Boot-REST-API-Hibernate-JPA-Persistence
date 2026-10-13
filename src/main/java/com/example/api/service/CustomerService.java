package com.example.api.service;

import com.example.api.dto.request.CustomerRequestDTO;
import com.example.api.dto.response.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {

    CustomerResponseDTO createCustomer(CustomerRequestDTO request);

    List<CustomerResponseDTO> getAllCustomers();

    CustomerResponseDTO getCustomerById(Long id);

    CustomerResponseDTO getCustomerWithOrders(Long id);

    List<CustomerResponseDTO> searchCustomers(String keyword);

    CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO request);

    void deleteCustomer(Long id);
}
