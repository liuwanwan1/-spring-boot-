package com.warehouse.controller;

import com.warehouse.entity.Customer;
import com.warehouse.service.CustomerService;
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
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public String listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Customer> customerPage;
        
        if (name != null && !name.isEmpty()) {
            customerPage = customerService.searchCustomers(name, null, pageable);
        } else {
            customerPage = customerService.getAllCustomers(pageable);
        }
        
        model.addAttribute("customers", customerPage);
        model.addAttribute("name", name);
        
        return "customers/list";
    }

    @GetMapping("/new")
    public String newCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customers/form";
    }

    @PostMapping
    public String createCustomer(@ModelAttribute Customer customer) {
        customerService.createCustomer(customer);
        return "redirect:/customers";
    }

    @GetMapping("/{id}/edit")
    public String editCustomerForm(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.getCustomerById(id));
        return "customers/form";
    }

    @PostMapping("/{id}")
    public String updateCustomer(@PathVariable Long id, @ModelAttribute Customer customer) {
        customerService.updateCustomer(id, customer);
        return "redirect:/customers";
    }

    @GetMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return "redirect:/customers";
    }

    @GetMapping("/{id}")
    public String viewCustomer(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.getCustomerById(id));
        return "customers/detail";
    }

    // API端点
    @RestController
    @RequestMapping("/api/customers")
    @RequiredArgsConstructor
    public static class CustomerApiController {
        
        private final CustomerService customerService;

        @GetMapping
        public ResponseEntity<Page<Customer>> getAllCustomers(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(customerService.getAllCustomers(pageable));
        }

        @GetMapping("/active")
        public ResponseEntity<List<Customer>> getActiveCustomers() {
            return ResponseEntity.ok(customerService.getActiveCustomers());
        }

        @GetMapping("/{id}")
        public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
            return ResponseEntity.ok(customerService.getCustomerById(id));
        }

        @PostMapping
        public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
            return ResponseEntity.ok(customerService.createCustomer(customer));
        }

        @PutMapping("/{id}")
        public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
            return ResponseEntity.ok(customerService.updateCustomer(id, customer));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok().build();
        }
    }
}
