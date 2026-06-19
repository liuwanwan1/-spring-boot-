package com.warehouse.controller;

import com.warehouse.dto.ProductDTO;
import com.warehouse.entity.Product;
import com.warehouse.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage;
        
        if (name != null || category != null || status != null) {
            productPage = productService.searchProducts(name, category, status, pageable);
        } else {
            productPage = productService.getAllProducts(pageable);
        }
        
        model.addAttribute("products", productPage);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("name", name);
        model.addAttribute("category", category);
        model.addAttribute("status", status);
        
        return "products/list";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new ProductDTO());
        model.addAttribute("categories", productService.getAllCategories());
        return "products/form";
    }

    @PostMapping
    public String createProduct(@ModelAttribute ProductDTO productDTO) {
        productService.createProduct(productDTO);
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        ProductDTO dto = ProductDTO.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .brand(product.getBrand())
                .specification(product.getSpecification())
                .purchasePrice(product.getPurchasePrice())
                .sellingPrice(product.getSellingPrice())
                .stockQuantity(product.getStockQuantity())
                .minStock(product.getMinStock())
                .maxStock(product.getMaxStock())
                .unit(product.getUnit())
                .imageUrl(product.getImageUrl())
                .status(product.getStatus().name())
                .build();
        model.addAttribute("product", dto);
        model.addAttribute("categories", productService.getAllCategories());
        return "products/form";
    }

    @PostMapping("/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute ProductDTO productDTO) {
        productService.updateProduct(id, productDTO);
        return "redirect:/products";
    }

    @GetMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        return "products/detail";
    }

    @GetMapping("/low-stock")
    public String lowStockProducts(Model model) {
        model.addAttribute("products", productService.getLowStockProducts());
        return "products/low-stock";
    }

    // API端点
    @RestController
    @RequestMapping("/api/products")
    @RequiredArgsConstructor
    public static class ProductApiController {
        
        private final ProductService productService;

        @GetMapping
        public ResponseEntity<Page<Product>> getAllProducts(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(productService.getAllProducts(pageable));
        }

        @GetMapping("/{id}")
        public ResponseEntity<Product> getProduct(@PathVariable Long id) {
            return ResponseEntity.ok(productService.getProductById(id));
        }

        @PostMapping
        public ResponseEntity<Product> createProduct(@RequestBody ProductDTO dto) {
            return ResponseEntity.ok(productService.createProduct(dto));
        }

        @PutMapping("/{id}")
        public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductDTO dto) {
            return ResponseEntity.ok(productService.updateProduct(id, dto));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        }

        @GetMapping("/search")
        public ResponseEntity<List<Product>> searchByName(@RequestParam String name) {
            return ResponseEntity.ok(productService.searchByName(name));
        }
    }
}
