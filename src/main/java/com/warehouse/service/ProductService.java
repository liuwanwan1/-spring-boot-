package com.warehouse.service;

import com.warehouse.dto.ProductDTO;
import com.warehouse.entity.Product;
import com.warehouse.entity.Product.ProductStatus;
import com.warehouse.exception.BusinessException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public Product createProduct(ProductDTO dto) {
        if (productRepository.existsByCode(dto.getCode())) {
            throw new BusinessException("商品编码已存在: " + dto.getCode());
        }
        
        Product product = Product.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .brand(dto.getBrand())
                .specification(dto.getSpecification())
                .purchasePrice(dto.getPurchasePrice())
                .sellingPrice(dto.getSellingPrice())
                .stockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0)
                .minStock(dto.getMinStock() != null ? dto.getMinStock() : 10)
                .maxStock(dto.getMaxStock() != null ? dto.getMaxStock() : 1000)
                .unit(dto.getUnit())
                .imageUrl(dto.getImageUrl())
                .status(ProductStatus.ACTIVE)
                .build();
        
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Transactional(readOnly = true)
    public Product getProductByCode(String code) {
        return productRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "code", code));
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        return productRepository.findByStatus(ProductStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Transactional(readOnly = true)
    public long getLowStockCount() {
        return productRepository.countLowStock();
    }

    public Product updateProduct(Long id, ProductDTO dto) {
        Product product = getProductById(id);
        
        // 检查编码是否被其他商品使用
        if (!product.getCode().equals(dto.getCode()) && productRepository.existsByCode(dto.getCode())) {
            throw new BusinessException("商品编码已存在: " + dto.getCode());
        }
        
        product.setCode(dto.getCode());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setBrand(dto.getBrand());
        product.setSpecification(dto.getSpecification());
        product.setPurchasePrice(dto.getPurchasePrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setMinStock(dto.getMinStock());
        product.setMaxStock(dto.getMaxStock());
        product.setUnit(dto.getUnit());
        product.setImageUrl(dto.getImageUrl());
        
        if (dto.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(dto.getStatus()));
        }
        
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String name, String category, String status, Pageable pageable) {
        ProductStatus productStatus = status != null ? ProductStatus.valueOf(status) : null;
        return productRepository.searchProducts(name, category, productStatus, pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return productRepository.findAll().stream()
                .map(Product::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    // 调整库存
    public Product adjustStock(Long id, Integer quantity, String reason) {
        Product product = getProductById(id);
        int newStock = product.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new BusinessException("库存不足，无法调整");
        }
        product.setStockQuantity(newStock);
        return productRepository.save(product);
    }
}
