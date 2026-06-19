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
public class SalesOrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productCode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String remark;
}
