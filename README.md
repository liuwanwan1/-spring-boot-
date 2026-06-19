# 仓库进销存管理系统 (WMS)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> 企业级仓库进销存管理系统，基于 Spring Boot 3.x 构建，支持商品管理、采购管理、销售管理、库存管理和数据统计分析。

## 功能特性

### 核心模块

- **仪表盘** - 实时数据统计、库存预警、待处理订单
- **商品管理** - 商品编码、分类、品牌、价格、库存管理
- **供应商管理** - 供应商信息、联系人、银行账户管理
- **客户管理** - 客户信息、信用等级、信用额度管理
- **采购管理** - 采购订单、采购入库、采购退货
- **销售管理** - 销售订单、销售出库、销售退货
- **库存管理** - 库存预警、库存调整、库存流水记录

### 技术特性

- 基于 Spring Boot 3.2 + Java 17
- Spring Security 安全认证与权限控制
- Spring Data JPA 数据持久化
- Thymeleaf 模板引擎 + 响应式前端
- H2 内存数据库（开发）/ MySQL（生产）
- RESTful API 设计
- 数据初始化与异常处理

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- （可选）MySQL 8.0+

### 运行方式

```bash
# 1. 克隆项目
git clone https://github.com/liuwanwan1/-spring-boot-.git

# 2. 进入项目目录
cd warehouse-management-system

# 3. 编译运行
mvn spring-boot:run

# 4. 访问系统
# 浏览器打开 http://localhost:8080
# 默认账号: admin / admin123
```

### 打包部署

```bash
# 打包为可执行 JAR
mvn clean package

# 运行 JAR
java -jar target/warehouse-management-system-1.0.0.jar
```

## 数据库配置

### 开发环境（默认 H2）

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:warehouse_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console
```

### 生产环境（MySQL）

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/warehouse_db?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

## 系统架构

```
warehouse-management-system
├── src/main/java/com/warehouse/
│   ├── config/          # 配置类（Security、数据初始化）
│   ├── controller/      # 控制器层（Web + API）
│   ├── dto/             # 数据传输对象
│   ├── entity/          # 实体类（JPA）
│   ├── exception/       # 异常处理
│   ├── repository/      # 数据访问层（JPA Repository）
│   └── service/         # 业务逻辑层
├── src/main/resources/
│   ├── templates/       # Thymeleaf 页面模板
│   └── application.yml  # 应用配置
└── pom.xml              # Maven 配置
```

## 实体关系

- **Product** (商品) - 库存管理、价格管理
- **Supplier** (供应商) - 采购关联
- **Customer** (客户) - 销售关联
- **PurchaseOrder** / **PurchaseOrderItem** - 采购订单/明细
- **SalesOrder** / **SalesOrderItem** - 销售订单/明细
- **InventoryTransaction** - 库存流水记录
- **User** - 用户与权限管理

## API 接口

系统提供完整的 RESTful API：

| 接口 | 说明 |
|------|------|
| `GET /api/products` | 商品列表 |
| `POST /api/products` | 创建商品 |
| `GET /api/dashboard/stats` | 仪表盘统计 |
| `GET /api/purchase-orders` | 采购订单列表 |
| `GET /api/sales-orders` | 销售订单列表 |

更多接口详见 Controller 代码。

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user | user123 |

## 技术栈

- **后端**: Spring Boot, Spring Security, Spring Data JPA, Hibernate
- **前端**: Thymeleaf, HTML5, CSS3, JavaScript
- **数据库**: H2 (开发) / MySQL (生产)
- **构建工具**: Maven
- **其他**: Lombok, BCrypt

## 开发计划

- [x] 基础架构搭建
- [x] 用户认证与权限管理
- [x] 商品管理模块
- [x] 供应商/客户管理
- [x] 采购/销售订单管理
- [x] 库存管理与预警
- [x] 仪表盘统计
- [ ] 报表导出（Excel/PDF）
- [ ] 数据图表可视化
- [ ] 操作日志审计

## 贡献指南

欢迎提交 Issue 和 Pull Request。

## 许可证

本项目基于 MIT 许可证开源。

---

> 基于 Spring Boot 的企业级仓库进销存管理系统 | 2024
