package com.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String code; // 商品编码
    
    @Column(nullable = false, length = 100)
    private String name; // 商品名称
    
    @Column(length = 255)
    private String description; // 商品描述
    
    @Column(length = 50)
    private String category; // 分类
    
    @Column(length = 50)
    private String brand; // 品牌
    
    @Column(length = 50)
    private String specification; // 规格
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice; // 进货价
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice; // 销售价
    
    @Column(nullable = false)
    private Integer stockQuantity; // 库存数量
    
    @Column(nullable = false)
    private Integer minStock; // 最低库存预警
    
    @Column(nullable = false)
    private Integer maxStock; // 最高库存限制
    
    @Column(length = 50)
    private String unit; // 单位
    
    @Column(length = 255)
    private String imageUrl; // 图片URL
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status; // 状态
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = ProductStatus.ACTIVE;
        }
        if (stockQuantity == null) {
            stockQuantity = 0;
        }
        if (minStock == null) {
            minStock = 10;
        }
        if (maxStock == null) {
            maxStock = 1000;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ProductStatus {
        ACTIVE, INACTIVE, DISCONTINUED
    }
    
    // 判断库存是否低于预警线
    public boolean isLowStock() {
        return stockQuantity <= minStock;
    }
    
    // 判断库存是否充足
    public boolean isStockSufficient(int quantity) {
        return stockQuantity >= quantity;
    }
}
