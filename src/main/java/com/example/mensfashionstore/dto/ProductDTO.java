package com.example.mensfashionstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
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

    private String description;

    private String imageUrl;

    @NotNull(message = "Stock quantity is required")
    private Integer stockQuantity;

    private LocalDateTime createdAt;

    public String getDisplayImageUrl() {
        if (imageUrl != null && !imageUrl.isEmpty() && (imageUrl.startsWith("http") || imageUrl.startsWith("/"))) {
            return imageUrl;
        }
        return "/images/placeholder.png";
    }

    public boolean isOutOfStock() {
        return stockQuantity != null && stockQuantity <= 0;
    }
}
