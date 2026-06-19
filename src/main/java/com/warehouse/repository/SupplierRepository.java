package com.warehouse.repository;

import com.warehouse.entity.Supplier;
import com.warehouse.entity.Supplier.SupplierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    Optional<Supplier> findByName(String name);
    
    List<Supplier> findByStatus(SupplierStatus status);
    
    Page<Supplier> findByStatus(SupplierStatus status, Pageable pageable);
    
    List<Supplier> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT s FROM Supplier s WHERE " +
           "(:name IS NULL OR s.name LIKE %:name%) AND " +
           "(:status IS NULL OR s.status = :status)")
    Page<Supplier> searchSuppliers(
        @Param("name") String name,
        @Param("status") SupplierStatus status,
        Pageable pageable
    );
    
    boolean existsByName(String name);
}
