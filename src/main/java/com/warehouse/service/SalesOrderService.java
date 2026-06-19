package com.warehouse.service;

import com.warehouse.dto.SalesOrderDTO;
import com.warehouse.dto.SalesOrderItemDTO;
import com.warehouse.entity.*;
import com.warehouse.entity.SalesOrder.OrderStatus;
import com.warehouse.entity.InventoryTransaction.TransactionType;
import com.warehouse.exception.BusinessException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    public SalesOrder createOrder(SalesOrderDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", dto.getCustomerId()));

        SalesOrder order = SalesOrder.builder()
                .orderNo(generateOrderNo())
                .customer(customer)
                .totalAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .deliveryDate(dto.getDeliveryDate() != null ? 
                    LocalDateTime.parse(dto.getDeliveryDate() + "T00:00:00") : null)
                .remark(dto.getRemark())
                .build();

        order = salesOrderRepository.save(order);

        // 添加明细
        if (dto.getItems() != null) {
            for (SalesOrderItemDTO itemDTO : dto.getItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDTO.getProductId()));

                // 检查库存
                if (!product.isStockSufficient(itemDTO.getQuantity())) {
                    throw new BusinessException("商品库存不足: " + product.getName() + 
                        " (可用: " + product.getStockQuantity() + ", 需要: " + itemDTO.getQuantity() + ")");
                }

                SalesOrderItem item = SalesOrderItem.builder()
                        .salesOrder(order)
                        .product(product)
                        .quantity(itemDTO.getQuantity())
                        .unitPrice(itemDTO.getUnitPrice())
                        .remark(itemDTO.getRemark())
                        .build();
                item.calculateSubtotal();
                order.addItem(item);
            }
            order.recalculateTotal();
        }

        return salesOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public SalesOrder getOrderById(Long id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", id));
    }

    @Transactional(readOnly = true)
    public SalesOrder getOrderByNo(String orderNo) {
        return salesOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "orderNo", orderNo));
    }

    @Transactional(readOnly = true)
    public Page<SalesOrder> getAllOrders(Pageable pageable) {
        return salesOrderRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<SalesOrder> getOrdersByStatus(OrderStatus status) {
        return salesOrderRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return salesOrderRepository.countByStatus(OrderStatus.PENDING);
    }

    public SalesOrder updateOrder(Long id, SalesOrderDTO dto) {
        SalesOrder order = getOrderById(id);
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("只能修改待处理的订单");
        }

        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", dto.getCustomerId()));
            order.setCustomer(customer);
        }

        if (dto.getDeliveryDate() != null) {
            order.setDeliveryDate(LocalDateTime.parse(dto.getDeliveryDate() + "T00:00:00"));
        }
        order.setRemark(dto.getRemark());

        // 更新明细
        if (dto.getItems() != null) {
            order.getItems().clear();
            for (SalesOrderItemDTO itemDTO : dto.getItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDTO.getProductId()));

                if (!product.isStockSufficient(itemDTO.getQuantity())) {
                    throw new BusinessException("商品库存不足: " + product.getName());
                }

                SalesOrderItem item = SalesOrderItem.builder()
                        .salesOrder(order)
                        .product(product)
                        .quantity(itemDTO.getQuantity())
                        .unitPrice(itemDTO.getUnitPrice())
                        .remark(itemDTO.getRemark())
                        .build();
                item.calculateSubtotal();
                order.addItem(item);
            }
            order.recalculateTotal();
        }

        return salesOrderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        SalesOrder order = getOrderById(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("只能删除待处理的订单");
        }
        salesOrderRepository.delete(order);
    }

    // 审批订单
    public SalesOrder approveOrder(Long id) {
        SalesOrder order = getOrderById(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("只能审批待处理的订单");
        }
        order.setStatus(OrderStatus.APPROVED);
        return salesOrderRepository.save(order);
    }

    // 发货出库
    public SalesOrder shipOrder(Long id) {
        SalesOrder order = getOrderById(id);
        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BusinessException("只能对已审批的订单进行发货");
        }

        // 更新库存并记录交易
        for (SalesOrderItem item : order.getItems()) {
            Product product = item.getProduct();
            
            if (!product.isStockSufficient(item.getQuantity())) {
                throw new BusinessException("商品库存不足: " + product.getName() + 
                    " (可用: " + product.getStockQuantity() + ", 需要: " + item.getQuantity() + ")");
            }

            int stockBefore = product.getStockQuantity();
            product.setStockQuantity(stockBefore - item.getQuantity());
            productRepository.save(product);

            // 记录库存交易
            InventoryTransaction transaction = InventoryTransaction.builder()
                    .product(product)
                    .type(TransactionType.SALES_OUT)
                    .quantity(-item.getQuantity())
                    .stockBefore(stockBefore)
                    .stockAfter(product.getStockQuantity())
                    .referenceNo(order.getOrderNo())
                    .referenceType("SALES")
                    .remark("销售出库: " + order.getOrderNo())
                    .operator("system")
                    .build();
            inventoryTransactionRepository.save(transaction);
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setDeliveryDate(LocalDateTime.now());
        return salesOrderRepository.save(order);
    }

    // 完成订单
    public SalesOrder completeOrder(Long id) {
        SalesOrder order = getOrderById(id);
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new BusinessException("只能对已发货的订单进行完成操作");
        }
        order.setStatus(OrderStatus.COMPLETED);
        return salesOrderRepository.save(order);
    }

    // 取消订单
    public SalesOrder cancelOrder(Long id) {
        SalesOrder order = getOrderById(id);
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("已完成的订单无法取消");
        }
        
        // 如果已发货，需要回滚库存
        if (order.getStatus() == OrderStatus.SHIPPED) {
            for (SalesOrderItem item : order.getItems()) {
                Product product = item.getProduct();
                int stockBefore = product.getStockQuantity();
                product.setStockQuantity(stockBefore + item.getQuantity());
                productRepository.save(product);

                InventoryTransaction transaction = InventoryTransaction.builder()
                        .product(product)
                        .type(TransactionType.SALES_RETURN)
                        .quantity(item.getQuantity())
                        .stockBefore(stockBefore)
                        .stockAfter(product.getStockQuantity())
                        .referenceNo(order.getOrderNo())
                        .referenceType("SALES")
                        .remark("销售退货: " + order.getOrderNo())
                        .operator("system")
                        .build();
                inventoryTransactionRepository.save(transaction);
            }
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        return salesOrderRepository.save(order);
    }

    // 收款
    public SalesOrder payOrder(Long id, BigDecimal amount) {
        SalesOrder order = getOrderById(id);
        BigDecimal remaining = order.getRemainingAmount();
        if (amount.compareTo(remaining) > 0) {
            throw new BusinessException("收款金额不能超过剩余金额");
        }
        order.setPaidAmount(order.getPaidAmount().add(amount));
        return salesOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Page<SalesOrder> searchOrders(String orderNo, String status, 
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status) : null;
        return salesOrderRepository.searchSalesOrders(orderNo, orderStatus, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTodaySalesAmount() {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime tomorrow = today.plusDays(1);
        return salesOrderRepository.sumTotalAmountByDateRange(today, tomorrow);
    }

    @Transactional(readOnly = true)
    public BigDecimal getMonthSalesAmount() {
        LocalDateTime firstDay = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime nextMonth = firstDay.plusMonths(1);
        return salesOrderRepository.sumTotalAmountByDateRange(firstDay, nextMonth);
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = salesOrderRepository.count() + 1;
        return "SO" + dateStr + String.format("%04d", count);
    }
}
