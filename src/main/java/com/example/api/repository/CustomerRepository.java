package com.example.api.repository;

import com.example.api.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Derived query methods
    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    // Custom JPQL query for searching customers by name or email keyword
    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Customer> searchByKeyword(@Param("keyword") String keyword);

    // Custom JPQL query with JOIN FETCH to optimize lazy loading and prevent N+1 queries
    @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.orders WHERE c.id = :id")
    Optional<Customer> findByIdWithOrders(@Param("id") Long id);

    // Custom Native query to retrieve recently created customers
    @Query(value = "SELECT * FROM customers WHERE created_at >= :sinceDate ORDER BY created_at DESC", nativeQuery = true)
    List<Customer> findRecentCustomersNative(@Param("sinceDate") LocalDateTime sinceDate);
}
