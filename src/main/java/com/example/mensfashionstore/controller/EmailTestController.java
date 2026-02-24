package com.example.mensfashionstore.controller;

import com.example.mensfashionstore.model.Order;
import com.example.mensfashionstore.service.EmailService;
import com.example.mensfashionstore.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Profile("dev")
public class EmailTestController {
    private static final Logger log = LoggerFactory.getLogger(EmailTestController.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/test-email/{orderId}")
    @ResponseBody
    public String testEmail(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            log.info("Email test endpoint called for orderId={}, userEmail={}",
                    orderId, order.getUser().getEmail());
            
            emailService.sendOrderConfirmationEmail(order);
            
            return "Email sending initiated for Order #" + orderId + 
                   " to " + order.getUser().getEmail() + 
                   ". Check console for details.";
        } catch (Exception e) {
            log.error("Email test endpoint failed for orderId={}", orderId, e);
            return "Error: " + e.getMessage();
        }
    }
}
