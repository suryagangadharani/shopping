package com.example.mensfashionstore.controller;

import com.example.mensfashionstore.model.Product;
import com.example.mensfashionstore.model.User;
import com.example.mensfashionstore.service.ProductService;
import com.example.mensfashionstore.service.UserService;
import com.example.mensfashionstore.service.FileUploadService;
import com.example.mensfashionstore.service.DashboardService;
import com.example.mensfashionstore.service.OrderService;
import com.example.mensfashionstore.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            var stats = dashboardService.getDashboardStats();
            model.addAttribute("stats", stats);
            model.addAttribute("products", productService.listProducts(0, 100, "asc", null).getContent());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("orders", orderService.findAll());
            return "admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load dashboard: " + e.getMessage());
            return "error";
        }
    }

    // ==================== PRODUCT MANAGEMENT ====================

    @GetMapping("/products")
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            Model model) {
        var products = productService.listProducts(page, size, sortDir, search);
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("sortDir", sortDir);
        return "admin/products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/add_product";
    }

    @PostMapping("/products")
    public String addProduct(
            @Valid @ModelAttribute Product product,
            BindingResult bindingResult,
            @RequestParam(name = "productImage", required = false) MultipartFile file,
            @RequestParam(required = false) String imageUrl,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "admin/add_product";
        }

        try {
            // Handle file upload
            if (file != null && !file.isEmpty()) {
                String uploadedImageUrl = fileUploadService.saveFile(file);
                product.setImageUrl(uploadedImageUrl);
            } else if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                String normalizedImageUrl = normalizeImageUrl(imageUrl);
                if (normalizedImageUrl.startsWith("/uploads/")) {
                    product.setImageUrl(normalizedImageUrl);
                } else if (fileUploadService.isValidImageUrl(normalizedImageUrl)) {
                    String downloadedImageUrl = fileUploadService.saveImageFromUrl(normalizedImageUrl);
                    product.setImageUrl(downloadedImageUrl);
                } else {
                    throw new IllegalArgumentException("Invalid image URL. Please use a direct image link.");
                }
            } else {
                product.setImageUrl("/images/placeholder.svg");
            }

            productService.save(product);
            redirectAttributes.addFlashAttribute("success", "Product added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding product: " + e.getMessage());
        }

        String redirectTo = "/admin/products";
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/admin/dashboard")) {
            redirectTo = "/admin/dashboard";
        }
        return "redirect:" + redirectTo;
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        try {
            Product product = productService.getProductById(id);
            model.addAttribute("product", product);
            return "admin/edit_product";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/products/{id}/edit")
    public String editProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute Product productDetails,
            BindingResult bindingResult,
            @RequestParam(name = "productImage", required = false) MultipartFile file,
            @RequestParam(required = false) String imageUrl,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "admin/edit_product";
        }

        try {
            Product existingProduct = productService.getProductById(id);
            existingProduct.setName(productDetails.getName());
            existingProduct.setBrand(productDetails.getBrand());
            existingProduct.setCategory(productDetails.getCategory());
            existingProduct.setPrice(productDetails.getPrice());
            existingProduct.setDescription(productDetails.getDescription());
            existingProduct.setStockQuantity(productDetails.getStockQuantity());

            // Handle file upload
            if (file != null && !file.isEmpty()) {
                // Delete old file if it exists
                if (existingProduct.getImageUrl() != null && existingProduct.getImageUrl().startsWith("/uploads/")) {
                    fileUploadService.deleteFile(existingProduct.getImageUrl());
                }
                String uploadedImageUrl = fileUploadService.saveFile(file);
                existingProduct.setImageUrl(uploadedImageUrl);
            } else if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                String normalizedImageUrl = normalizeImageUrl(imageUrl);
                if (normalizedImageUrl.startsWith("/uploads/")) {
                    existingProduct.setImageUrl(normalizedImageUrl);
                } else if (fileUploadService.isValidImageUrl(normalizedImageUrl)) {
                    if (existingProduct.getImageUrl() != null && existingProduct.getImageUrl().startsWith("/uploads/")) {
                        fileUploadService.deleteFile(existingProduct.getImageUrl());
                    }
                    String downloadedImageUrl = fileUploadService.saveImageFromUrl(normalizedImageUrl);
                    existingProduct.setImageUrl(downloadedImageUrl);
                } else {
                    throw new IllegalArgumentException("Invalid image URL. Please use a direct image link.");
                }
            }

            productService.save(existingProduct);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating product: " + e.getMessage());
        }

        String redirectTo = "/admin/products";
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/admin/dashboard")) {
            redirectTo = "/admin/dashboard";
        }
        return "redirect:" + redirectTo;
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id);
            if (product.getImageUrl() != null && product.getImageUrl().startsWith("/uploads/")) {
                fileUploadService.deleteFile(product.getImageUrl());
            }
            productService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (IllegalArgumentException e) {
            // Foreign key constraint error
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting product: " + e.getMessage());
        }
        String redirectTo = "/admin/products";
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/admin/dashboard")) {
            redirectTo = "/admin/dashboard#productsTab";
        }
        return "redirect:" + redirectTo;
    }

    @PostMapping("/products/bulk-delete")
    public String bulkDeleteProducts(@RequestParam("ids") java.util.List<Long> ids,
            @RequestParam(value = "activeTab", required = false, defaultValue = "productsTab") String activeTab,
            RedirectAttributes redirectAttributes) {
        try {
            int deletedCount = 0;
            for (Long id : ids) {
                try {
                    Product product = productService.getProductById(id);
                    if (product.getImageUrl() != null && product.getImageUrl().startsWith("/uploads/")) {
                        fileUploadService.deleteFile(product.getImageUrl());
                    }
                    productService.deleteById(id);
                    deletedCount++;
                } catch (Exception e) {
                    // Continue with next product if one fails
                    continue;
                }
            }
            if (deletedCount > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    deletedCount + " product(s) deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Failed to delete products.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error deleting products: " + e.getMessage());
        }
        return "redirect:/admin/dashboard#" + activeTab;
    }

    @PostMapping("/products/{id}/stock")
    public String updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            RedirectAttributes redirectAttributes) {
        try {
            productService.updateStock(id, quantity);
            redirectAttributes.addFlashAttribute("success", "Stock updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating stock: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/users/add")
    public String addUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/add_user";
    }

    @PostMapping("/users")
    public String addUser(
            @Valid @ModelAttribute User user,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "admin/add_user";
        }

        try {
            if (userService.emailExists(user.getEmail())) {
                bindingResult.rejectValue("email", "error.user", "Email already exists");
                return "admin/add_user";
            }

            user.setIsActive(true);
            userService.save(user);
            redirectAttributes.addFlashAttribute("success", "User created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating user: " + e.getMessage());
        }
        String redirectTo = "/admin/users";
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/admin/dashboard")) {
            redirectTo = "/admin/dashboard";
        }
        return "redirect:" + redirectTo;
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        try {
            User user = userService.getUserById(id);
            model.addAttribute("user", user);
            return "admin/edit_user";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/{id}/edit")
    public String editUser(
            @PathVariable Long id,
            @Valid @ModelAttribute User userDetails,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "admin/edit_user";
        }

        try {
            userService.updateUser(id, userDetails);
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
        }
        String redirectTo = "/admin/users";
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/admin/dashboard")) {
            redirectTo = "/admin/dashboard";
        }
        return "redirect:" + redirectTo;
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        String redirectTo = "/admin/users";
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/admin/dashboard")) {
            redirectTo = "/admin/dashboard#usersTab";
        }
        return "redirect:" + redirectTo;
    }

    @PostMapping("/users/bulk-delete")
    public String bulkDeleteUsers(@RequestParam("ids") java.util.List<Long> ids,
            @RequestParam(value = "activeTab", required = false, defaultValue = "usersTab") String activeTab,
            RedirectAttributes redirectAttributes) {
        try {
            int deletedCount = 0;
            for (Long id : ids) {
                try {
                    userService.deleteUser(id);
                    deletedCount++;
                } catch (Exception e) {
                    // Continue with next user if one fails
                    continue;
                }
            }
            if (deletedCount > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    deletedCount + " user(s) deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Failed to delete users.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error deleting users: " + e.getMessage());
        }
        return "redirect:/admin/dashboard#" + activeTab;
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("success", "User status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user status: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/change-role")
    public String changeUserRole(
            @PathVariable Long id,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {
        try {
            userService.changeUserRole(id, role);
            redirectAttributes.addFlashAttribute("success", "User role changed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error changing user role: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ==================== ORDER MANAGEMENT ====================

    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.findAll());
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        try {
            var order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            return "admin/order_detail";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam com.example.mensfashionstore.model.OrderStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Order status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating order status: " + e.getMessage());
        }
        return "redirect:/admin/orders";

    }

    @PostMapping("/orders/{id}/delete")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("success", "Order deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete order: " + e.getMessage());
        }
        return "redirect:/admin/dashboard#ordersTab";
    }

    @PostMapping("/orders/bulk-delete")
    public String bulkDeleteOrders(@RequestParam("ids") java.util.List<Long> ids,
            @RequestParam(value = "activeTab", required = false, defaultValue = "ordersTab") String activeTab,
            RedirectAttributes redirectAttributes) {
        try {
            int deletedCount = 0;
            for (Long id : ids) {
                try {
                    orderService.deleteOrder(id);
                    deletedCount++;
                } catch (Exception e) {
                    // Continue with next order if one fails
                    continue;
                }
            }
            if (deletedCount > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    deletedCount + " order(s) deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Failed to delete orders.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error deleting orders: " + e.getMessage());
        }
        return "redirect:/admin/dashboard#" + activeTab;
    }

    private String normalizeImageUrl(String imageUrl) {
        String normalized = imageUrl.trim();
        if (normalized.startsWith("www.")) {
            return "https://" + normalized;
        }
        if (normalized.startsWith("//")) {
            return "https:" + normalized;
        }
        return normalized;
    }
}

