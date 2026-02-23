package com.example.mensfashionstore.controller;

import com.example.mensfashionstore.model.Order;
import com.example.mensfashionstore.service.EmailService;
import com.example.mensfashionstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/test-email/{orderId}")
    @ResponseBody
    public String testEmail(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            
            System.out.println("========================================");
            System.out.println("TEST EMAIL ENDPOINT CALLED");
            System.out.println("Order ID: " + orderId);
            System.out.println("User Email: " + order.getUser().getEmail());
            System.out.println("========================================");
            
            emailService.sendOrderConfirmationEmail(order);
            
            return "Email sending initiated for Order #" + orderId + 
                   " to " + order.getUser().getEmail() + 
                   ". Check console for details.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
