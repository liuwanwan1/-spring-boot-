package com.warehouse.repository;

import com.warehouse.entity.InventoryTransaction;
import com.warehouse.entity.InventoryTransaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    
    List<InventoryTransaction> findByProductId(Long productId);
    
    List<InventoryTransaction> findByType(TransactionType type);
    
    @Query("SELECT it FROM InventoryTransaction it WHERE it.product.id = :productId ORDER BY it.transactionTime DESC")
    List<InventoryTransaction> findByProductIdOrderByTimeDesc(@Param("productId") Long productId);
    
    @Query("SELECT it FROM InventoryTransaction it WHERE " +
           "(:productId IS NULL OR it.product.id = :productId) AND " +
           "(:type IS NULL OR it.type = :type) AND " +
           "(:startDate IS NULL OR it.transactionTime >= :startDate) AND " +
           "(:endDate IS NULL OR it.transactionTime <= :endDate)")
    Page<InventoryTransaction> searchTransactions(
        @Param("productId") Long productId,
        @Param("type") TransactionType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    @Query("SELECT SUM(it.quantity) FROM InventoryTransaction it WHERE it.product.id = :productId")
    Integer getStockQuantityByProductId(@Param("productId") Long productId);
    
    @Query("SELECT it.product.id, SUM(it.quantity) FROM InventoryTransaction it GROUP BY it.product.id")
    List<Object[]> getStockQuantities();
}
