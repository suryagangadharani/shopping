package com.example.mensfashionstore.controller;

import com.example.mensfashionstore.service.ProductService;
import com.example.mensfashionstore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import com.example.mensfashionstore.model.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CartService cartService;

    @GetMapping({"/", "/products"})
    public String home(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       @RequestParam(defaultValue = "asc") String sort,
                       @RequestParam(required = false) String q,
                       @RequestParam(required = false) String category) {
        Page<Product> products;
        if (category != null && !category.isBlank()) {
            products = productService.findByCategory(category, page, size, sort);
        } else {
            products = productService.listProducts(page, size, sort, q);
        }
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("search", q);
        model.addAttribute("sort", sort);
        model.addAttribute("category", category);
        
        // Add cart item count for the badge
        int cartItemCount = cartService.getCartItems().size();
        model.addAttribute("cartItemCount", cartItemCount);
        
        return "index";
    }
}
