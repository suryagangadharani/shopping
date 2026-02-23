package com.example.mensfashionstore.controller;

import com.example.mensfashionstore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice to add common attributes to all views
 */
@ControllerAdvice
public class GlobalControllerAdvice {
    
    @Autowired
    private CartService cartService;
    
    /**
     * Adds cart item count to all views automatically
     * This ensures the cart badge always shows the correct count
     */
    @ModelAttribute
    public void addCartCount(Model model) {
        try {
            var items = cartService.getCartItems();
            int cartItemCount = items.size();
            
            // DEBUG: Print to console to verify this is running
            System.out.println("🛒 GlobalControllerAdvice - Cart Item Count: " + cartItemCount);
            System.out.println("🛒 Cart Items: " + items);
            
            model.addAttribute("cartItemCount", cartItemCount);
        } catch (Exception e) {
            // If there's any error, default to 0
            System.out.println("❌ GlobalControllerAdvice Error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("cartItemCount", 0);
        }
    }
}
