package com.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder; // 所属销售单
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 商品
    
    @Column(nullable = false)
    private Integer quantity; // 数量
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice; // 单价
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal; // 小计
    
    @Column(length = 255)
    private String remark; // 备注
    
    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
