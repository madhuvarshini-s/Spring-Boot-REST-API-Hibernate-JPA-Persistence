package com.example.api.service.impl;

import com.example.api.dto.request.CustomerRequestDTO;
import com.example.api.dto.response.CustomerResponseDTO;
import com.example.api.dto.response.OrderResponseDTO;
import com.example.api.entity.Customer;
import com.example.api.entity.Order;
import com.example.api.exception.DuplicateResourceException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.repository.CustomerRepository;
import com.example.api.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A customer with email '" + request.getEmail() + "' already exists.");
        }

        Customer customer = new Customer(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress()
        );

        Customer saved = customerRepository.save(customer);
        return mapToDTO(saved, false);
    }

    @Override
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customer -> mapToDTO(customer, false))
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return mapToDTO(customer, false);
    }

    @Override
    public CustomerResponseDTO getCustomerWithOrders(Long id) {
        Customer customer = customerRepository.findByIdWithOrders(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return mapToDTO(customer, true);
    }

    @Override
    public List<CustomerResponseDTO> searchCustomers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllCustomers();
        }
        return customerRepository.searchByKeyword(keyword).stream()
                .map(customer -> mapToDTO(customer, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        if (customerRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("A customer with email '" + request.getEmail() + "' already exists.");
        }

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        Customer updated = customerRepository.save(customer);
        return mapToDTO(updated, false);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customerRepository.delete(customer);
    }

    private CustomerResponseDTO mapToDTO(Customer customer, boolean includeOrders) {
        CustomerResponseDTO dto = new CustomerResponseDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );

        if (includeOrders && customer.getOrders() != null) {
            List<OrderResponseDTO> orderDTOs = customer.getOrders().stream()
                    .map(this::mapOrderToDTO)
                    .collect(Collectors.toList());
            dto.setOrders(orderDTOs);
        }

        return dto;
    }

    private OrderResponseDTO mapOrderToDTO(Order order) {
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
