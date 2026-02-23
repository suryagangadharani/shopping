package com.example.mensfashionstore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String orderStatus;
    private List<OrderItemDTO> items;

    public Double calculateTotal() {
        if (items == null) {
            return 0.0;
        }
        return items.stream()
                .mapToDouble(OrderItemDTO::getSubtotal)
                .sum();
    }
}
