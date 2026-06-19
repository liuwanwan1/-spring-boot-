package com.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name; // 客户名称
    
    @Column(length = 50)
    private String contactPerson; // 联系人
    
    @Column(length = 20)
    private String phone; // 电话
    
    @Column(length = 100)
    private String email; // 邮箱
    
    @Column(length = 255)
    private String address; // 地址
    
    @Column(length = 50)
    private String creditLevel; // 信用等级
    
    @Column(precision = 10, scale = 2)
    private BigDecimal creditLimit; // 信用额度
    
    @Column(length = 500)
    private String remark; // 备注
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomerStatus status; // 状态
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = CustomerStatus.ACTIVE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum CustomerStatus {
        ACTIVE, INACTIVE
    }
}
