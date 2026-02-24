package com.example.mensfashionstore.controller;

import com.example.mensfashionstore.model.User;
import com.example.mensfashionstore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegister(Model model, Authentication authentication) {
        if (isLoggedIn(authentication)) {
            return "redirect:/";
        }
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult br,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (br.hasErrors()) {
            return "register";
        }

        if (userService.emailExists(user.getEmail())) {
            br.rejectValue("email", "error.user", "Email already registered");
            return "register";
        }

        try {
            user.setRole("USER");
            user.setIsActive(true);
            user.setRegistrationDate(LocalDateTime.now());
            userService.save(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLogin(Authentication authentication) {
        if (isLoggedIn(authentication)) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/login?error")
    public String loginError(Model model) {
        model.addAttribute("error", "Invalid credentials");
        return "login";
    }

    private boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
