package com.example.mensfashionstore.controller;

import com.example.mensfashionstore.model.Address;
import com.example.mensfashionstore.model.Order;
import com.example.mensfashionstore.model.OrderItem;
import com.example.mensfashionstore.model.OrderStatus;
import com.example.mensfashionstore.model.Product;
import com.example.mensfashionstore.model.User;
import com.example.mensfashionstore.service.CartService;
import com.example.mensfashionstore.service.EmailService;
import com.example.mensfashionstore.service.OrderService;
import com.example.mensfashionstore.service.ProductService;
import com.example.mensfashionstore.service.RazorpayService;
import com.example.mensfashionstore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private EmailService emailService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    
    @GetMapping("")
    public String viewCart(Model model) {
        var items = cartService.getCartItems();
        var total = cartService.getCartTotal();
        
        model.addAttribute("cartItems", items);
        model.addAttribute("cartTotal", total);
        model.addAttribute("cartItemCount", items.size());
        
        return "cart";
    }


    @PostMapping("/add/{id}")
    public String addToCart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer qty,
            @RequestParam(defaultValue = "false") boolean buyNow,
            RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id);

            // Validate quantity
            if (qty <= 0) {
                redirectAttributes.addFlashAttribute("error", "Quantity must be greater than 0");
                return "redirect:/product/" + id;
            }

            if (product.getStockQuantity() == null || qty > product.getStockQuantity()) {
                redirectAttributes.addFlashAttribute("error", "Not enough stock available.");
                return "redirect:/product/" + id;
            }

            cartService.addToCart(product, qty);
            redirectAttributes.addFlashAttribute("success", "Product added to cart!");

            if (buyNow) {
                return "redirect:/cart/checkout";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding product to cart: " + e.getMessage());
        }
        return "redirect:/product/" + id;
    }

    @PostMapping("/remove/{id}")
    public String removeFromCart(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.removeFromCart(id);
            redirectAttributes.addFlashAttribute("success", "Product removed from cart");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error removing product: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update/{id}")
    public String updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.updateQuantity(id, quantity);
            redirectAttributes.addFlashAttribute("success", "Cart updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating cart: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        var items = cartService.getCartItems();

        if (items.isEmpty()) {
            return "redirect:/cart";
        }

        if (userDetails == null) {
            return "redirect:/login";
        }

        var total = cartService.getCartTotal();
        model.addAttribute("cartItems", items);
        model.addAttribute("cartTotal", total);
        model.addAttribute("cartItemCount", items.size());
        model.addAttribute("address", new Address()); // Populate empty address for form
        return "checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("address") Address address,
            BindingResult bindingResult,
            @RequestParam String mobileNumber,
            @RequestParam String paymentMethod,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            // Re-populate model for view
            var items = cartService.getCartItems();
            var total = cartService.getCartTotal();
            model.addAttribute("cartItems", items);
            model.addAttribute("cartTotal", total);
            model.addAttribute("error", "Please fix the errors in the address form.");
            return "checkout";
        }

        var items = cartService.getCartItems();
        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cart is empty");
            return "redirect:/cart";
        }

        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<OrderItem> orderItems = new ArrayList<>();

            for (CartService.CartItem cartItem : items) {
                Product product = productService.getProductById(cartItem.getProductId());

                if (product.getStockQuantity() == null || product.getStockQuantity() < cartItem.getQuantity()) {
                    throw new RuntimeException("Product " + product.getName() + " is out of stock.");
                }

                productService.decreaseStock(product.getId(), cartItem.getQuantity());

                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(cartItem.getPrice());

                orderItems.add(orderItem);
            }

            Order savedOrder = orderService.createOrder(user, orderItems, address, mobileNumber, paymentMethod);
            cartService.clearCart();

            // Logic for Razorpay
            if ("CARD".equals(paymentMethod) || "RAZORPAY".equals(paymentMethod)) { // Assuming 'CARD' is used for
                                                                                    // online payment
                com.razorpay.Order razorpayOrder = razorpayService.createOrder(savedOrder.getTotalAmount());
                savedOrder.setRazorpayOrderId(razorpayOrder.get("id"));
                savedOrder.setOrderStatus(OrderStatus.PENDING); // Wait for payment
                orderService.save(savedOrder);

                // Show payment gateway page
                model.addAttribute("order", savedOrder);
                model.addAttribute("razorpayOrderId", savedOrder.getRazorpayOrderId());
                model.addAttribute("razorpayKeyId", razorpayKeyId);
                model.addAttribute("currency", "INR");
                model.addAttribute("amount", (int) (savedOrder.getTotalAmount() * 100));

                return "payment_gateway";
            }

            // For COD
            try {
                emailService.sendOrderConfirmationEmail(savedOrder);
            } catch (Exception e) {
                // Log error but don't fail the order
                System.err.println("Failed to send email: " + e.getMessage());
            }
            redirectAttributes.addFlashAttribute("success", "Order placed successfully!");
            return "redirect:/order/" + savedOrder.getId();
        } catch (Exception e) {
            // Restore stock logic is missing here but usually in a transaction so it should
            // rollback
            // Ideally should handle transaction rollback explicitly if not using
            // @Transactional
            redirectAttributes.addFlashAttribute("error", "Error placing order: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }
}
