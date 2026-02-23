package com.example.mensfashionstore.repository;

import com.example.mensfashionstore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.product.id = :productId")
    List<Order> findByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(o) FROM Order o")
    long countTotalOrders();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderStatus <> com.example.mensfashionstore.model.OrderStatus.CANCELLED")
    Double calculateTotalRevenue();
}
