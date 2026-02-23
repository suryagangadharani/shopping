package com.example.mensfashionstore.controller;

import com.example.mensfashionstore.model.Product;
import com.example.mensfashionstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Optional<Product> p = productService.findById(id);
        if (p.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("product", p.get());
        return "product_detail";
    }
}
