package com.warehouse.repository;

import com.warehouse.entity.PurchaseOrder;
import com.warehouse.entity.PurchaseOrder.OrderStatus;
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
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    
    Optional<PurchaseOrder> findByOrderNo(String orderNo);
    
    List<PurchaseOrder> findByStatus(OrderStatus status);
    
    Page<PurchaseOrder> findByStatus(OrderStatus status, Pageable pageable);
    
    @Query("SELECT po FROM PurchaseOrder po WHERE po.supplier.id = :supplierId")
    List<PurchaseOrder> findBySupplierId(@Param("supplierId") Long supplierId);
    
    @Query("SELECT po FROM PurchaseOrder po WHERE " +
           "(:orderNo IS NULL OR po.orderNo LIKE %:orderNo%) AND " +
           "(:status IS NULL OR po.status = :status) AND " +
           "(:startDate IS NULL OR po.orderDate >= :startDate) AND " +
           "(:endDate IS NULL OR po.orderDate <= :endDate)")
    Page<PurchaseOrder> searchPurchaseOrders(
        @Param("orderNo") String orderNo,
        @Param("status") OrderStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.status = :status")
    long countByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT SUM(po.totalAmount) FROM PurchaseOrder po WHERE po.status = 'COMPLETED' AND po.orderDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumTotalAmountByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
