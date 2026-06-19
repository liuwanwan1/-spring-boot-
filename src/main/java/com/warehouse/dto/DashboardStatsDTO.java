package com.warehouse.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private long totalProducts;
    private long totalSuppliers;
    private long totalCustomers;
    private long pendingPurchaseOrders;
    private long pendingSalesOrders;
    private long lowStockCount;
    private BigDecimal todayPurchaseAmount;
    private BigDecimal todaySalesAmount;
    private BigDecimal monthPurchaseAmount;
    private BigDecimal monthSalesAmount;
}
