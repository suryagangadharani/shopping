package com.example.mensfashionstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DebugController {
    
    @GetMapping("/debug/cart")
    public String cartDebug() {
        return "cart_debug";
    }
    
    @GetMapping("/debug/badge")
    public String badgeTest() {
        return "cart_badge_test";
    }
}
