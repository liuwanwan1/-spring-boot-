package com.warehouse.controller;

import com.warehouse.entity.Supplier;
import com.warehouse.service.SupplierService;
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
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public String listSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Supplier> supplierPage;
        
        if (name != null && !name.isEmpty()) {
            supplierPage = supplierService.searchSuppliers(name, null, pageable);
        } else {
            supplierPage = supplierService.getAllSuppliers(pageable);
        }
        
        model.addAttribute("suppliers", supplierPage);
        model.addAttribute("name", name);
        
        return "suppliers/list";
    }

    @GetMapping("/new")
    public String newSupplierForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        return "suppliers/form";
    }

    @PostMapping
    public String createSupplier(@ModelAttribute Supplier supplier) {
        supplierService.createSupplier(supplier);
        return "redirect:/suppliers";
    }

    @GetMapping("/{id}/edit")
    public String editSupplierForm(@PathVariable Long id, Model model) {
        model.addAttribute("supplier", supplierService.getSupplierById(id));
        return "suppliers/form";
    }

    @PostMapping("/{id}")
    public String updateSupplier(@PathVariable Long id, @ModelAttribute Supplier supplier) {
        supplierService.updateSupplier(id, supplier);
        return "redirect:/suppliers";
    }

    @GetMapping("/{id}/delete")
    public String deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return "redirect:/suppliers";
    }

    @GetMapping("/{id}")
    public String viewSupplier(@PathVariable Long id, Model model) {
        model.addAttribute("supplier", supplierService.getSupplierById(id));
        return "suppliers/detail";
    }

    // API端点
    @RestController
    @RequestMapping("/api/suppliers")
    @RequiredArgsConstructor
    public static class SupplierApiController {
        
        private final SupplierService supplierService;

        @GetMapping
        public ResponseEntity<Page<Supplier>> getAllSuppliers(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(supplierService.getAllSuppliers(pageable));
        }

        @GetMapping("/active")
        public ResponseEntity<List<Supplier>> getActiveSuppliers() {
            return ResponseEntity.ok(supplierService.getActiveSuppliers());
        }

        @GetMapping("/{id}")
        public ResponseEntity<Supplier> getSupplier(@PathVariable Long id) {
            return ResponseEntity.ok(supplierService.getSupplierById(id));
        }

        @PostMapping
        public ResponseEntity<Supplier> createSupplier(@RequestBody Supplier supplier) {
            return ResponseEntity.ok(supplierService.createSupplier(supplier));
        }

        @PutMapping("/{id}")
        public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
            return ResponseEntity.ok(supplierService.updateSupplier(id, supplier));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
            supplierService.deleteSupplier(id);
            return ResponseEntity.ok().build();
        }
    }
}
