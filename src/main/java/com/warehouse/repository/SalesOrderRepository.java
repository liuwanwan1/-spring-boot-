package com.warehouse.repository;

import com.warehouse.entity.SalesOrder;
import com.warehouse.entity.SalesOrder.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    
    Optional<SalesOrder> findByOrderNo(String orderNo);
    
    List<SalesOrder> findByStatus(OrderStatus status);
    
    Page<SalesOrder> findByStatus(OrderStatus status, Pageable pageable);
    
    @Query("SELECT so FROM SalesOrder so WHERE so.customer.id = :customerId")
    List<SalesOrder> findByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT so FROM SalesOrder so WHERE " +
           "(:orderNo IS NULL OR so.orderNo LIKE %:orderNo%) AND " +
           "(:status IS NULL OR so.status = :status) AND " +
           "(:startDate IS NULL OR so.orderDate >= :startDate) AND " +
           "(:endDate IS NULL OR so.orderDate <= :endDate)")
    Page<SalesOrder> searchSalesOrders(
        @Param("orderNo") String orderNo,
        @Param("status") OrderStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    @Query("SELECT COUNT(so) FROM SalesOrder so WHERE so.status = :status")
    long countByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT SUM(so.totalAmount) FROM SalesOrder so WHERE so.status = 'COMPLETED' AND so.orderDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumTotalAmountByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
