package com.example.mensfashionstore.service;

import com.example.mensfashionstore.model.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SessionScope
public class CartService implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<Long, CartItem> cartItems = new HashMap<>();

    public void addToCart(Product product, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        if (product.getStockQuantity() == null || product.getStockQuantity() <= 0) {
            throw new IllegalArgumentException("Product is out of stock");
        }

        if (product.getPrice() == null) {
            throw new IllegalArgumentException("Product price cannot be null");
        }

        Long productId = product.getId();

        if (cartItems.containsKey(productId)) {
            CartItem item = cartItems.get(productId);
            int newQuantity = item.getQuantity() + quantity;

            if (newQuantity > product.getStockQuantity()) {
                throw new IllegalArgumentException(
                        "Only " + product.getStockQuantity() + " items available in stock");
            }

            item.setQuantity(newQuantity);
        } else {
            if (quantity > product.getStockQuantity()) {
                throw new IllegalArgumentException(
                        "Only " + product.getStockQuantity() + " items available in stock");
            }

            CartItem item = CartItem.builder()
                    .productId(productId)
                    .productName(product.getName())
                    .productImage(product.getDisplayImageUrl())
                    .price(product.getPrice())
                    .quantity(quantity)
                    .build();

            cartItems.put(productId, item);
        }
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems.values());
    }

    public double getCartTotal() {
        return cartItems.values().stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    public void removeFromCart(Long productId) {
        cartItems.remove(productId);
    }

    public void updateQuantity(Long productId, Integer quantity) {
        if (quantity <= 0) {
            removeFromCart(productId);
        } else {
            if (cartItems.containsKey(productId)) {
                CartItem item = cartItems.get(productId);
                item.setQuantity(quantity);
            }
        }
    }

    public void clearCart() {
        cartItems.clear();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long productId;
        private String productName;
        private String productImage;
        private Double price;
        private Integer quantity;

        public double getSubtotal() {
            if (price == null || quantity == null) {
                return 0.0;
            }
            return price * quantity;
        }
    }
}
