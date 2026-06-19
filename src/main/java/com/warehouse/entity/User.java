package com.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username; // 用户名
    
    @Column(nullable = false, length = 100)
    private String password; // 密码（加密存储）
    
    @Column(nullable = false, length = 100)
    private String realName; // 真实姓名
    
    @Column(length = 20)
    private String phone; // 电话
    
    @Column(length = 100)
    private String email; // 邮箱
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role; // 角色
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status; // 状态
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime lastLoginTime; // 最后登录时间
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (role == null) {
            role = Role.USER;
        }
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }
    
    public enum Role {
        ADMIN,      // 管理员
        MANAGER,    // 经理
        OPERATOR,   // 操作员
        USER        // 普通用户
    }
    
    public enum UserStatus {
        ACTIVE, INACTIVE, LOCKED
    }
    
    // 检查是否为管理员
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    // 检查是否为经理或管理员
    public boolean isManagerOrAdmin() {
        return role == Role.ADMIN || role == Role.MANAGER;
    }
}
