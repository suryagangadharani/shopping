package com.example.mensfashionstore.controller;

import com.example.mensfashionstore.model.Order;
import com.example.mensfashionstore.model.OrderStatus;
import com.example.mensfashionstore.model.Payment;
import com.example.mensfashionstore.repository.PaymentRepository;
import com.example.mensfashionstore.service.EmailService;
import com.example.mensfashionstore.service.OrderService;
import com.example.mensfashionstore.service.RazorpayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/verify")
    public String verifyPayment(
            @RequestParam("razorpay_order_id") String razorpayOrderId,
            @RequestParam("razorpay_payment_id") String razorpayPaymentId,
            @RequestParam("razorpay_signature") String razorpaySignature,
            @RequestParam("order_id") Long dbOrderId,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            boolean isValid = razorpayService.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);

            if (isValid) {
                // Update Order
                Order order = orderService.getOrderById(dbOrderId);
                order.setRazorpayOrderId(razorpayOrderId);
                order.setRazorpayPaymentId(razorpayPaymentId);
                order.setPaymentStatus("PAID");
                order.setOrderStatus(OrderStatus.PLACED); // Confirm the order
                orderService.save(order);

                // Save Payment Record
                Payment payment = new Payment();
                payment.setOrderId(dbOrderId);
                payment.setPaymentId(razorpayPaymentId);
                payment.setRazorpayOrderId(razorpayOrderId);
                payment.setAmount(order.getTotalAmount());
                payment.setCurrency("INR");
                payment.setStatus("SUCCESS");
                payment.setMethod("RAZORPAY");
                paymentRepository.save(payment);

                // Send Email
                try {
                    emailService.sendOrderConfirmationEmail(order);
                } catch (Exception e) {
                    log.warn("Failed to trigger order confirmation email for orderId={}", dbOrderId, e);
                }

                // Show Success Page
                model.addAttribute("order", order);
                model.addAttribute("paymentId", razorpayPaymentId);
                return "payment_success";
            } else {
                redirectAttributes.addFlashAttribute("error", "Payment verification failed. Signature mismatch.");
                return "redirect:/cart/checkout";
            }

        } catch (Exception e) {
            log.error("Payment verification flow failed for dbOrderId={}", dbOrderId, e);
            redirectAttributes.addFlashAttribute("error", "Payment verification failed: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }
}
