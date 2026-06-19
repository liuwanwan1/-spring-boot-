package com.warehouse.controller;

import com.warehouse.dto.DashboardStatsDTO;
import com.warehouse.entity.InventoryTransaction;
import com.warehouse.entity.Product;
import com.warehouse.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/")
    public String redirectToDashboard() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        List<Product> lowStockProducts = dashboardService.getLowStockProducts();
        List<InventoryTransaction> recentTransactions = dashboardService.getRecentTransactions(10);
        
        model.addAttribute("stats", stats);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("recentTransactions", recentTransactions);
        
        return "dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // API端点
    @RestController
    @RequestMapping("/api/dashboard")
    @RequiredArgsConstructor
    public static class DashboardApiController {
        
        private final DashboardService dashboardService;

        @GetMapping("/stats")
        public ResponseEntity<DashboardStatsDTO> getStats() {
            return ResponseEntity.ok(dashboardService.getDashboardStats());
        }

        @GetMapping("/low-stock")
        public ResponseEntity<List<Product>> getLowStockProducts() {
            return ResponseEntity.ok(dashboardService.getLowStockProducts());
        }
    }
}
