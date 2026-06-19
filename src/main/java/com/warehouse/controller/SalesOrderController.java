package com.warehouse.controller;

import com.warehouse.entity.SalesOrder;
import com.warehouse.entity.SalesOrder.OrderStatus;
import com.warehouse.dto.SalesOrderDTO;
import com.warehouse.service.SalesOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sales-orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SalesOrder> orderPage = salesOrderService.searchOrders(orderNo, status, null, null, pageable);
        
        model.addAttribute("orders", orderPage);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("orderNo", orderNo);
        model.addAttribute("status", status);
        
        return "sales-orders/list";
    }

    @GetMapping("/new")
    public String newOrderForm(Model model) {
        model.addAttribute("order", new SalesOrderDTO());
        return "sales-orders/form";
    }

    @PostMapping
    public String createOrder(@ModelAttribute SalesOrderDTO orderDTO) {
        salesOrderService.createOrder(orderDTO);
        return "redirect:/sales-orders";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        model.addAttribute("order", salesOrderService.getOrderById(id));
        return "sales-orders/detail";
    }

    @GetMapping("/{id}/approve")
    public String approveOrder(@PathVariable Long id) {
        salesOrderService.approveOrder(id);
        return "redirect:/sales-orders/" + id;
    }

    @GetMapping("/{id}/ship")
    public String shipOrder(@PathVariable Long id) {
        salesOrderService.shipOrder(id);
        return "redirect:/sales-orders/" + id;
    }

    @GetMapping("/{id}/complete")
    public String completeOrder(@PathVariable Long id) {
        salesOrderService.completeOrder(id);
        return "redirect:/sales-orders/" + id;
    }

    @GetMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id) {
        salesOrderService.cancelOrder(id);
        return "redirect:/sales-orders/" + id;
    }

    @PostMapping("/{id}/pay")
    public String payOrder(@PathVariable Long id, @RequestParam BigDecimal amount) {
        salesOrderService.payOrder(id, amount);
        return "redirect:/sales-orders/" + id;
    }

    // API端点
    @RestController
    @RequestMapping("/api/sales-orders")
    @RequiredArgsConstructor
    public static class SalesOrderApiController {
        
        private final SalesOrderService salesOrderService;

        @GetMapping
        public ResponseEntity<Page<SalesOrder>> getAllOrders(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(salesOrderService.getAllOrders(pageable));
        }

        @GetMapping("/{id}")
        public ResponseEntity<SalesOrder> getOrder(@PathVariable Long id) {
            return ResponseEntity.ok(salesOrderService.getOrderById(id));
        }

        @PostMapping
        public ResponseEntity<SalesOrder> createOrder(@RequestBody SalesOrderDTO dto) {
            return ResponseEntity.ok(salesOrderService.createOrder(dto));
        }

        @PutMapping("/{id}")
        public ResponseEntity<SalesOrder> updateOrder(@PathVariable Long id, @RequestBody SalesOrderDTO dto) {
            return ResponseEntity.ok(salesOrderService.updateOrder(id, dto));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
            salesOrderService.deleteOrder(id);
            return ResponseEntity.ok().build();
        }

        @PostMapping("/{id}/approve")
        public ResponseEntity<SalesOrder> approveOrder(@PathVariable Long id) {
            return ResponseEntity.ok(salesOrderService.approveOrder(id));
        }

        @PostMapping("/{id}/ship")
        public ResponseEntity<SalesOrder> shipOrder(@PathVariable Long id) {
            return ResponseEntity.ok(salesOrderService.shipOrder(id));
        }

        @PostMapping("/{id}/complete")
        public ResponseEntity<SalesOrder> completeOrder(@PathVariable Long id) {
            return ResponseEntity.ok(salesOrderService.completeOrder(id));
        }

        @PostMapping("/{id}/cancel")
        public ResponseEntity<SalesOrder> cancelOrder(@PathVariable Long id) {
            return ResponseEntity.ok(salesOrderService.cancelOrder(id));
        }

        @PostMapping("/{id}/pay")
        public ResponseEntity<SalesOrder> payOrder(@PathVariable Long id, @RequestParam BigDecimal amount) {
            return ResponseEntity.ok(salesOrderService.payOrder(id, amount));
        }
    }
}
