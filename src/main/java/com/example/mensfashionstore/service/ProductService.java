package com.example.mensfashionstore.service;

import com.example.mensfashionstore.model.Product;
import com.example.mensfashionstore.model.Order;
import com.example.mensfashionstore.repository.ProductRepository;
import com.example.mensfashionstore.repository.OrderRepository;
import com.example.mensfashionstore.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    public Page<Product> listProducts(int page, int size, String sortDir, String search) {
        Sort sort = Sort.by("price");
        sort = "desc".equalsIgnoreCase(sortDir) ? sort.descending() : sort.ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        if (search != null && !search.isBlank()) {
            return productRepository.findByNameContainingIgnoreCase(search, pageable);
        }
        return productRepository.findAll(pageable);
    }

    public Page<Product> listProductsByCategory(String category, int page, int size, String sortDir) {
        Sort sort = Sort.by("price");
        sort = "desc".equalsIgnoreCase(sortDir) ? sort.descending() : sort.ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findByCategoryIgnoreCase(category, pageable);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with ID: " + id);
        }
        try {
            // Find all orders containing this product
            List<Order> ordersWithProduct = orderRepository.findByProductId(id);

            for (Order order : ordersWithProduct) {
                // Remove only the items that match this product
                // orphanRemoval=true on Order.items will cause these OrderItems to be deleted
                // from DB
                boolean removed = order.getItems()
                        .removeIf(item -> item.getProduct() != null && item.getProduct().getId().equals(id));

                if (removed) {
                    if (order.getItems().isEmpty()) {
                        // If order is empty after removing item, delete the order
                        orderRepository.delete(order);
                    } else {
                        // Otherwise update the order (recalculates total if needed, but Total is field
                        // in Order)
                        // Note: You might need to recalculate order total here if it's stored in DB
                        // But for now, just saving to trigger orphan removal
                        // order.setTotalAmount(order.calculateTotal()); // explicit recalculate if
                        // needed
                        // Recalculate order total to ensure consistency
                        order.setTotalAmount(order.calculateTotal());
                        orderRepository.save(order);
                    }
                }
            }

            // Force flush to ensure OrderItems are deleted before Product deletion
            // attempted
            orderRepository.flush();

            // Now delete the product
            productRepository.deleteById(id);
        } catch (Exception e) {
            // Catch DataIntegrityViolationException and others
            if (e.getMessage() != null && e.getMessage().contains("constraint")) {
                throw new IllegalArgumentException(
                        "Cannot delete product: Foreign key constraint failure. " +
                                "Ensure all references are cleared.",
                        e);
            }
            throw e;
        }
    }

    public Page<Product> findByCategory(String category, int page, int size, String sortDir) {
        Sort sort = Sort.by("price");
        sort = "desc".equalsIgnoreCase(sortDir) ? sort.descending() : sort.ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findByCategoryIgnoreCase(category, pageable);
    }

    public Product updateStock(Long id, Integer newStock) {
        Product product = getProductById(id);
        product.setStockQuantity(newStock);
        return save(product);
    }

    public Product decreaseStock(Long id, Integer quantity) {

        Product product = getProductById(id);

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid quantity");
        }

        if (product.getStockQuantity() == null) {
            throw new IllegalArgumentException("Stock quantity not set for product: " + product.getName());
        }

        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);

        return save(product);
    }

    public Product increaseStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        product.setStockQuantity(product.getStockQuantity() + quantity);
        return save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public long getTotalProductCount() {
        return productRepository.countTotalProducts();
    }

    public Double getTotalInventoryValue() {
        Double value = productRepository.calculateTotalInventoryValue();
        return value != null ? value : 0.0;
    }
}
