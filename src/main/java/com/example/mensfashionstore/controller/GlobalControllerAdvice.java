package com.example.mensfashionstore.controller;

import com.example.mensfashionstore.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice to add common attributes to all views.
 */
@ControllerAdvice
public class GlobalControllerAdvice {
    private static final Logger log = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @Autowired
    private CartService cartService;

    /**
     * Adds cart item count to all views automatically.
     */
    @ModelAttribute
    public void addCartCount(Model model) {
        try {
            int cartItemCount = cartService.getCartItems().size();
            model.addAttribute("cartItemCount", cartItemCount);
        } catch (Exception e) {
            // Default to zero so view rendering doesn't fail.
            log.warn("Failed to compute cart item count for model", e);
            model.addAttribute("cartItemCount", 0);
        }
    }
}
