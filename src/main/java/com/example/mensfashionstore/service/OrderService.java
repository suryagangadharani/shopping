package com.example.mensfashionstore.service;

import com.example.mensfashionstore.model.Address;
import com.example.mensfashionstore.model.Order;
import com.example.mensfashionstore.model.OrderItem;
import com.example.mensfashionstore.model.OrderStatus;
import com.example.mensfashionstore.model.User;
import com.example.mensfashionstore.repository.OrderRepository;
import com.example.mensfashionstore.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order createOrder(User user, List<OrderItem> items, Address address, String mobileNumber,
            String paymentMethod) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PLACED); // Changed default to PLACED on creation
        order.setShippingAddress(address);
        order.setMobileNumber(mobileNumber);
        order.setPaymentMethod(paymentMethod);

        for (OrderItem item : items) {
            item.setOrder(order);
            order.addItem(item);
        }

        Double totalAmount = order.calculateTotal();
        order.setTotalAmount(totalAmount);

        return save(order);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        order.setOrderStatus(newStatus);
        return save(order);
    }

    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.PLACED) {
            throw new IllegalArgumentException("Can only cancel pending or placed orders");
        }
        order.setOrderStatus(OrderStatus.CANCELLED);

        // Restore stock
        for (OrderItem item : order.getItems()) {
            productService.increaseStock(item.getProduct().getId(), item.getQuantity());
        }

        return save(order);
    }

    public long getTotalOrderCount() {
        return orderRepository.countTotalOrders();
    }

    public Double getTotalRevenue() {
        Double revenue = orderRepository.calculateTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }

    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with ID: " + id);
        }
        orderRepository.deleteById(id);
    }
}
