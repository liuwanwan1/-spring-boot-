package com.warehouse.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderDTO {
    private Long id;
    private String orderNo;
    private Long supplierId;
    private String supplierName;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private String status;
    private String orderDate;
    private String deliveryDate;
    private String remark;
    private List<PurchaseOrderItemDTO> items;
}
