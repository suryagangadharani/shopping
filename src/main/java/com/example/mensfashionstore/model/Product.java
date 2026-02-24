package com.example.mensfashionstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 1024)
    private String imageUrl;

    @NotNull(message = "Stock quantity is required")
    private Integer stockQuantity;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    public boolean isOutOfStock() {
        return stockQuantity <= 0;
    }

    public boolean hasValidImageUrl() {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return false;
        }
        try {
            new java.net.URL(imageUrl);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getDisplayImageUrl() {
        if (imageUrl == null) {
            return "/images/placeholder.svg";
        }

        String normalized = imageUrl.trim();
        if (normalized.isEmpty()) {
            return "/images/placeholder.svg";
        }

        if (normalized.startsWith("//")) {
            return "https:" + normalized;
        }

        if (normalized.startsWith("www.")) {
            return "https://" + normalized;
        }

        if (normalized.startsWith("http://") || normalized.startsWith("https://") || normalized.startsWith("/")) {
            return normalized;
        }
        return "/images/placeholder.svg";
    }
}

