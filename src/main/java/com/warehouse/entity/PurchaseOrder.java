package com.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String orderNo; // 采购单号
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier; // 供应商
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount; // 总金额
    
    @Column(precision = 12, scale = 2)
    private BigDecimal paidAmount; // 已付金额
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 状态
    
    @Column(nullable = false)
    private LocalDateTime orderDate; // 订单日期
    
    @Column
    private LocalDateTime deliveryDate; // 预计到货日期
    
    @Column(length = 500)
    private String remark; // 备注
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseOrderItem> items = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum OrderStatus {
        PENDING,    // 待处理
        APPROVED,   // 已审批
        RECEIVED,   // 已收货
        COMPLETED,  // 已完成
        CANCELLED   // 已取消
    }
    
    // 计算剩余应付金额
    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(paidAmount != null ? paidAmount : BigDecimal.ZERO);
    }
    
    // 添加明细项
    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
    }
    
    // 移除明细项
    public void removeItem(PurchaseOrderItem item) {
        items.remove(item);
        item.setPurchaseOrder(null);
    }
    
    // 重新计算总金额
    public void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(PurchaseOrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
