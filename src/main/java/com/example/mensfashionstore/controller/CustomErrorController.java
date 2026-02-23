package com.example.mensfashionstore.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");

        if (statusCode == null) {
            statusCode = 500;
        }

        // Build user-friendly error messages
        String userMessage = getErrorMessage(statusCode, errorMessage);
        
        model.addAttribute("status", statusCode);
        model.addAttribute("error", userMessage);
        
        if (throwable != null) {
            model.addAttribute("trace", throwable.getMessage());
        }

        return "error";
    }

    private String getErrorMessage(int statusCode, String message) {
        switch (statusCode) {
            case 400:
                return "Bad Request. Please check your input and try again.";
            case 403:
                return "Access Denied. You don't have permission to access this resource.";
            case 404:
                return "Page not found. The resource you're looking for doesn't exist.";
            case 500:
                return "Internal Server Error. Something went wrong on our end. Please try again later.";
            case 503:
                return "Service Unavailable. The server is temporarily unavailable. Please try again later.";
            default:
                return message != null ? message : "An unexpected error occurred. Please try again.";
        }
    }

    public String getErrorPath() {
        return "/error";
    }
}
