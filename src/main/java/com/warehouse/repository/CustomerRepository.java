package com.warehouse.repository;

import com.warehouse.entity.Customer;
import com.warehouse.entity.Customer.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByName(String name);
    
    List<Customer> findByStatus(CustomerStatus status);
    
    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);
    
    List<Customer> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Customer c WHERE " +
           "(:name IS NULL OR c.name LIKE %:name%) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<Customer> searchCustomers(
        @Param("name") String name,
        @Param("status") CustomerStatus status,
        Pageable pageable
    );
    
    boolean existsByName(String name);
}
