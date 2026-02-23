package com.example.mensfashionstore.controller;

import com.example.mensfashionstore.model.Product;
import com.example.mensfashionstore.service.ProductService;
import com.example.mensfashionstore.service.CartService;
import com.example.mensfashionstore.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductDetailController {
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CartService cartService;

    @GetMapping("/product/{id}")
    public String viewProductDetail(@PathVariable Long id, Model model) {
        try {
            Product product = productService.getProductById(id);
            model.addAttribute("product", product);
            
            // Add cart item count for the badge
            int cartItemCount = cartService.getCartItems().size();
            model.addAttribute("cartItemCount", cartItemCount);
            
            return "product_detail";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }
}
