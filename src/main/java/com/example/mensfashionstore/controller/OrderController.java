package com.example.mensfashionstore.controller;

import com.example.mensfashionstore.model.Order;
import com.example.mensfashionstore.model.User;
import com.example.mensfashionstore.service.OrderService;
import com.example.mensfashionstore.service.UserService;
import com.example.mensfashionstore.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping("/order/{id}")
    public String orderDetail(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Order order = orderService.getOrderById(id);
            
            // Verify user owns this order
            if (userDetails != null) {
                User user = userService.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                if (!order.getUser().getId().equals(user.getId()) && !userDetails.getAuthorities()
                        .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    return "redirect:/";
                }
            }
            
            model.addAttribute("order", order);
            return "order_summary";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/orders")
    public String myOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Order> orders = orderService.findByUserId(user.getId());
            model.addAttribute("orders", orders);
            return "orders";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load orders: " + e.getMessage());
            return "error";
        }
    }
}

