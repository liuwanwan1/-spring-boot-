package com.warehouse.service;

import com.warehouse.dto.DashboardStatsDTO;
import com.warehouse.entity.InventoryTransaction;
import com.warehouse.entity.Product;
import com.warehouse.entity.Product.ProductStatus;
import com.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final PurchaseOrderService purchaseOrderService;
    private final SalesOrderService salesOrderService;

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        stats.setTotalProducts(productRepository.count());
        stats.setTotalSuppliers(supplierRepository.count());
        stats.setTotalCustomers(customerRepository.count());
        stats.setPendingPurchaseOrders(purchaseOrderRepository.countByStatus(
            com.warehouse.entity.PurchaseOrder.OrderStatus.PENDING));
        stats.setPendingSalesOrders(salesOrderRepository.countByStatus(
            com.warehouse.entity.SalesOrder.OrderStatus.PENDING));
        stats.setLowStockCount(productRepository.countLowStock());
        
        BigDecimal todayPurchase = purchaseOrderService.getTodayPurchaseAmount();
        BigDecimal todaySales = salesOrderService.getTodaySalesAmount();
        BigDecimal monthPurchase = purchaseOrderService.getMonthPurchaseAmount();
        BigDecimal monthSales = salesOrderService.getMonthSalesAmount();
        
        stats.setTodayPurchaseAmount(todayPurchase != null ? todayPurchase : BigDecimal.ZERO);
        stats.setTodaySalesAmount(todaySales != null ? todaySales : BigDecimal.ZERO);
        stats.setMonthPurchaseAmount(monthPurchase != null ? monthPurchase : BigDecimal.ZERO);
        stats.setMonthSalesAmount(monthSales != null ? monthSales : BigDecimal.ZERO);
        
        return stats;
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public List<InventoryTransaction> getRecentTransactions(int limit) {
        return inventoryTransactionRepository.findAll(
            org.springframework.data.domain.PageRequest.of(0, limit, 
                org.springframework.data.domain.Sort.by("transactionTime").descending())
        ).getContent();
    }
}
