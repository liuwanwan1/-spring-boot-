package com.warehouse.repository;

import com.warehouse.entity.Product;
import com.warehouse.entity.Product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findByCode(String code);
    
    List<Product> findByStatus(ProductStatus status);
    
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    
    List<Product> findByCategory(String category);
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minStock AND p.status = 'ACTIVE'")
    List<Product> findLowStockProducts();
    
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR p.name LIKE %:name%) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<Product> searchProducts(
        @Param("name") String name,
        @Param("category") String category,
        @Param("status") ProductStatus status,
        Pageable pageable
    );
    
    @Query("SELECT p.category, COUNT(p) FROM Product p GROUP BY p.category")
    List<Object[]> countByCategory();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= p.minStock AND p.status = 'ACTIVE'")
    long countLowStock();
    
    boolean existsByCode(String code);
}
