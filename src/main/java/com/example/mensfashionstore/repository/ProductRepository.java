package com.example.mensfashionstore.repository;

import com.example.mensfashionstore.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p")
    long countTotalProducts();
    
    @Query("SELECT SUM(p.price * p.stockQuantity) FROM Product p")
    Double calculateTotalInventoryValue();
}
