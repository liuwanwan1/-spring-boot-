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
public class ProductDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String category;
    private String brand;
    private String specification;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private Integer stockQuantity;
    private Integer minStock;
    private Integer maxStock;
    private String unit;
    private String imageUrl;
    private String status;
    private boolean lowStock;
}
