package com.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 商品
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type; // 交易类型
    
    @Column(nullable = false)
    private Integer quantity; // 数量（正数表示入库，负数表示出库）
    
    @Column(nullable = false)
    private Integer stockBefore; // 变动前库存
    
    @Column(nullable = false)
    private Integer stockAfter; // 变动后库存
    
    @Column(length = 50)
    private String referenceNo; // 关联单号（采购单号/销售单号）
    
    @Column(length = 50)
    private String referenceType; // 关联类型（PURCHASE/SALES）
    
    @Column(length = 255)
    private String remark; // 备注
    
    @Column(length = 50)
    private String operator; // 操作人
    
    @Column(nullable = false)
    private LocalDateTime transactionTime; // 交易时间
    
    @PrePersist
    protected void onCreate() {
        if (transactionTime == null) {
            transactionTime = LocalDateTime.now();
        }
    }
    
    public enum TransactionType {
        PURCHASE_IN,    // 采购入库
        PURCHASE_RETURN, // 采购退货
        SALES_OUT,      // 销售出库
        SALES_RETURN,   // 销售退货
        ADJUSTMENT,     // 库存调整
        INITIAL         // 初始库存
    }
}
