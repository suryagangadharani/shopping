package com.example.mensfashionstore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private String paymentId; // Razorpay payment ID

    private String razorpayOrderId;

    private Double amount;

    private String currency;

    private String status;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String method; // e.g., card, netbanking, wallet
}
