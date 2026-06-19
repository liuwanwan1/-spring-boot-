package com.warehouse.service;

import com.warehouse.dto.PurchaseOrderDTO;
import com.warehouse.dto.PurchaseOrderItemDTO;
import com.warehouse.entity.*;
import com.warehouse.entity.PurchaseOrder.OrderStatus;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    public PurchaseOrder createOrder(PurchaseOrderDTO dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", dto.getSupplierId()));

        PurchaseOrder order = PurchaseOrder.builder()
                .orderNo(generateOrderNo())
                .supplier(supplier)
                .totalAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .deliveryDate(dto.getDeliveryDate() != null ? 
                    LocalDateTime.parse(dto.getDeliveryDate() + "T00:00:00") : null)
                .remark(dto.getRemark())
                .build();

        order = purchaseOrderRepository.save(order);

        // 添加明细
        if (dto.getItems() != null) {
            for (PurchaseOrderItemDTO itemDTO : dto.getItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDTO.getProductId()));

                PurchaseOrderItem item = PurchaseOrderItem.builder()
                        .purchaseOrder(order)
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

        return purchaseOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getOrderByNo(String orderNo) {
        return purchaseOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "orderNo", orderNo));
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrder> getAllOrders(Pageable pageable) {
        return purchaseOrderRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOrdersByStatus(OrderStatus status) {
        return purchaseOrderRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return purchaseOrderRepository.countByStatus(OrderStatus.PENDING);
    }

    public PurchaseOrder updateOrder(Long id, PurchaseOrderDTO dto) {
        PurchaseOrder order = getOrderById(id);
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("只能修改待处理的订单");
        }

        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", dto.getSupplierId()));
            order.setSupplier(supplier);
        }

        if (dto.getDeliveryDate() != null) {
            order.setDeliveryDate(LocalDateTime.parse(dto.getDeliveryDate() + "T00:00:00"));
        }
        order.setRemark(dto.getRemark());

        // 更新明细
        if (dto.getItems() != null) {
            order.getItems().clear();
            for (PurchaseOrderItemDTO itemDTO : dto.getItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDTO.getProductId()));

                PurchaseOrderItem item = PurchaseOrderItem.builder()
                        .purchaseOrder(order)
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

        return purchaseOrderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        PurchaseOrder order = getOrderById(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("只能删除待处理的订单");
        }
        purchaseOrderRepository.delete(order);
    }

    // 审批订单
    public PurchaseOrder approveOrder(Long id) {
        PurchaseOrder order = getOrderById(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("只能审批待处理的订单");
        }
        order.setStatus(OrderStatus.APPROVED);
        return purchaseOrderRepository.save(order);
    }

    // 收货入库
    public PurchaseOrder receiveOrder(Long id) {
        PurchaseOrder order = getOrderById(id);
        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new BusinessException("只能对已审批的订单进行收货");
        }

        // 更新库存并记录交易
        for (PurchaseOrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int stockBefore = product.getStockQuantity();
            product.setStockQuantity(stockBefore + item.getQuantity());
            productRepository.save(product);

            // 记录库存交易
            InventoryTransaction transaction = InventoryTransaction.builder()
                    .product(product)
                    .type(TransactionType.PURCHASE_IN)
                    .quantity(item.getQuantity())
                    .stockBefore(stockBefore)
                    .stockAfter(product.getStockQuantity())
                    .referenceNo(order.getOrderNo())
                    .referenceType("PURCHASE")
                    .remark("采购入库: " + order.getOrderNo())
                    .operator("system")
                    .build();
            inventoryTransactionRepository.save(transaction);
        }

        order.setStatus(OrderStatus.RECEIVED);
        return purchaseOrderRepository.save(order);
    }

    // 完成订单
    public PurchaseOrder completeOrder(Long id) {
        PurchaseOrder order = getOrderById(id);
        if (order.getStatus() != OrderStatus.RECEIVED) {
            throw new BusinessException("只能对已收货的订单进行完成操作");
        }
        order.setStatus(OrderStatus.COMPLETED);
        return purchaseOrderRepository.save(order);
    }

    // 取消订单
    public PurchaseOrder cancelOrder(Long id) {
        PurchaseOrder order = getOrderById(id);
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("已完成的订单无法取消");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return purchaseOrderRepository.save(order);
    }

    // 付款
    public PurchaseOrder payOrder(Long id, BigDecimal amount) {
        PurchaseOrder order = getOrderById(id);
        BigDecimal remaining = order.getRemainingAmount();
        if (amount.compareTo(remaining) > 0) {
            throw new BusinessException("付款金额不能超过剩余金额");
        }
        order.setPaidAmount(order.getPaidAmount().add(amount));
        return purchaseOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrder> searchOrders(String orderNo, String status, 
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status) : null;
        return purchaseOrderRepository.searchPurchaseOrders(orderNo, orderStatus, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTodayPurchaseAmount() {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime tomorrow = today.plusDays(1);
        return purchaseOrderRepository.sumTotalAmountByDateRange(today, tomorrow);
    }

    @Transactional(readOnly = true)
    public BigDecimal getMonthPurchaseAmount() {
        LocalDateTime firstDay = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime nextMonth = firstDay.plusMonths(1);
        return purchaseOrderRepository.sumTotalAmountByDateRange(firstDay, nextMonth);
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = purchaseOrderRepository.count() + 1;
        return "PO" + dateStr + String.format("%04d", count);
    }
}
