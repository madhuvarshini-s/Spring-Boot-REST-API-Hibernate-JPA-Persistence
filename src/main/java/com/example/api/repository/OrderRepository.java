package com.example.api.repository;

import com.example.api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Derived query methods
    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    boolean existsByOrderNumberAndIdNot(String orderNumber, Long id);

    // Custom JPQL query: find orders by customer ID
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.orderDate DESC")
    List<Order> findByCustomerId(@Param("customerId") Long customerId);

    // Custom JPQL query: filter orders with total amount greater than or equal to threshold
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount ORDER BY o.totalAmount DESC")
    List<Order> findOrdersAboveAmount(@Param("minAmount") Double minAmount);

    // Custom JPQL query: compute total spent by a specific customer
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.customer.id = :customerId")
    Double calculateTotalSpentByCustomer(@Param("customerId") Long customerId);

    // Custom Native query: filter by order status
    @Query(value = "SELECT * FROM orders WHERE status = :status ORDER BY order_date DESC", nativeQuery = true)
    List<Order> findByStatusNative(@Param("status") String status);
}
