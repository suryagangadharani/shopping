package com.example.mensfashionstore.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
                .totalUsers(userService.getTotalUserCount())
                .activeUsers(userService.getActiveUserCount())
                .totalProducts(productService.getTotalProductCount())
                .totalOrders(orderService.getTotalOrderCount())
                .totalRevenue(orderService.getTotalRevenue())
                .inventoryValue(productService.getTotalInventoryValue())
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardStats {
        private long totalUsers;
        private long activeUsers;
        private long totalProducts;
        private long totalOrders;
        private Double totalRevenue;
        private Double inventoryValue;
    }
}
