package com.warehouse.controller;

import com.warehouse.entity.PurchaseOrder;
import com.warehouse.entity.PurchaseOrder.OrderStatus;
import com.warehouse.dto.PurchaseOrderDTO;
import com.warehouse.service.PurchaseOrderService;
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
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PurchaseOrder> orderPage = purchaseOrderService.searchOrders(orderNo, status, null, null, pageable);
        
        model.addAttribute("orders", orderPage);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("orderNo", orderNo);
        model.addAttribute("status", status);
        
        return "purchase-orders/list";
    }

    @GetMapping("/new")
    public String newOrderForm(Model model) {
        model.addAttribute("order", new PurchaseOrderDTO());
        return "purchase-orders/form";
    }

    @PostMapping
    public String createOrder(@ModelAttribute PurchaseOrderDTO orderDTO) {
        purchaseOrderService.createOrder(orderDTO);
        return "redirect:/purchase-orders";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        model.addAttribute("order", purchaseOrderService.getOrderById(id));
        return "purchase-orders/detail";
    }

    @GetMapping("/{id}/approve")
    public String approveOrder(@PathVariable Long id) {
        purchaseOrderService.approveOrder(id);
        return "redirect:/purchase-orders/" + id;
    }

    @GetMapping("/{id}/receive")
    public String receiveOrder(@PathVariable Long id) {
        purchaseOrderService.receiveOrder(id);
        return "redirect:/purchase-orders/" + id;
    }

    @GetMapping("/{id}/complete")
    public String completeOrder(@PathVariable Long id) {
        purchaseOrderService.completeOrder(id);
        return "redirect:/purchase-orders/" + id;
    }

    @GetMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id) {
        purchaseOrderService.cancelOrder(id);
        return "redirect:/purchase-orders/" + id;
    }

    @PostMapping("/{id}/pay")
    public String payOrder(@PathVariable Long id, @RequestParam BigDecimal amount) {
        purchaseOrderService.payOrder(id, amount);
        return "redirect:/purchase-orders/" + id;
    }

    // API端点
    @RestController
    @RequestMapping("/api/purchase-orders")
    @RequiredArgsConstructor
    public static class PurchaseOrderApiController {
        
        private final PurchaseOrderService purchaseOrderService;

        @GetMapping
        public ResponseEntity<Page<PurchaseOrder>> getAllOrders(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(purchaseOrderService.getAllOrders(pageable));
        }

        @GetMapping("/{id}")
        public ResponseEntity<PurchaseOrder> getOrder(@PathVariable Long id) {
            return ResponseEntity.ok(purchaseOrderService.getOrderById(id));
        }

        @PostMapping
        public ResponseEntity<PurchaseOrder> createOrder(@RequestBody PurchaseOrderDTO dto) {
            return ResponseEntity.ok(purchaseOrderService.createOrder(dto));
        }

        @PutMapping("/{id}")
        public ResponseEntity<PurchaseOrder> updateOrder(@PathVariable Long id, @RequestBody PurchaseOrderDTO dto) {
            return ResponseEntity.ok(purchaseOrderService.updateOrder(id, dto));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
            purchaseOrderService.deleteOrder(id);
            return ResponseEntity.ok().build();
        }

        @PostMapping("/{id}/approve")
        public ResponseEntity<PurchaseOrder> approveOrder(@PathVariable Long id) {
            return ResponseEntity.ok(purchaseOrderService.approveOrder(id));
        }

        @PostMapping("/{id}/receive")
        public ResponseEntity<PurchaseOrder> receiveOrder(@PathVariable Long id) {
            return ResponseEntity.ok(purchaseOrderService.receiveOrder(id));
        }

        @PostMapping("/{id}/complete")
        public ResponseEntity<PurchaseOrder> completeOrder(@PathVariable Long id) {
            return ResponseEntity.ok(purchaseOrderService.completeOrder(id));
        }

        @PostMapping("/{id}/cancel")
        public ResponseEntity<PurchaseOrder> cancelOrder(@PathVariable Long id) {
            return ResponseEntity.ok(purchaseOrderService.cancelOrder(id));
        }

        @PostMapping("/{id}/pay")
        public ResponseEntity<PurchaseOrder> payOrder(@PathVariable Long id, @RequestParam BigDecimal amount) {
            return ResponseEntity.ok(purchaseOrderService.payOrder(id, amount));
        }
    }
}
