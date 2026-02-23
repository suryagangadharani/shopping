package com.example.mensfashionstore.config;

import com.example.mensfashionstore.model.Product;
import com.example.mensfashionstore.model.User;
import com.example.mensfashionstore.repository.ProductRepository;
import com.example.mensfashionstore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner init(UserRepository userRepository, ProductRepository productRepository, PasswordEncoder encoder) {
        return args -> {
            if (userRepository.findByEmail("admin@store.com").isEmpty()) {
                User admin = new User();
                admin.setFullName("Site Admin");
                admin.setEmail("admin@store.com");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole("ADMIN");
                admin.setRegistrationDate(LocalDateTime.now());
                userRepository.save(admin);
            }

            if (productRepository.count() == 0) {
                Product p1 = new Product();
                p1.setName("Classic Oxford Shirt");
                p1.setBrand("TailorMade");
                p1.setCategory("Shirts");
                p1.setPrice(49.99);
                p1.setDescription("Elegant classic oxford shirt for every occasion.");
                p1.setImageUrl("https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500");
                p1.setStockQuantity(50);

                Product p2 = new Product();
                p2.setName("Slim Fit Jeans");
                p2.setBrand("DenimCo");
                p2.setCategory("Jeans");
                p2.setPrice(69.99);
                p2.setDescription("Comfortable slim fit jeans with perfect fit.");
                p2.setImageUrl("https://images.unsplash.com/photo-1542272604-787c62d465d1?w=500");
                p2.setStockQuantity(40);

                Product p3 = new Product();
                p3.setName("Leather Jacket");
                p3.setBrand("UrbanStyle");
                p3.setCategory("Jackets");
                p3.setPrice(129.99);
                p3.setDescription("Premium leather jacket for a bold look.");
                p3.setImageUrl("https://images.unsplash.com/photo-1551028719-00167b16ebc5?w=500");
                p3.setStockQuantity(25);

                Product p4 = new Product();
                p4.setName("Running Sneakers");
                p4.setBrand("FastFeet");
                p4.setCategory("Shoes");
                p4.setPrice(89.99);
                p4.setDescription("Lightweight and comfortable running shoes.");
                p4.setImageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500");
                p4.setStockQuantity(35);

                Product p5 = new Product();
                p5.setName("Casual T-Shirt");
                p5.setBrand("CasualWear");
                p5.setCategory("Shirts");
                p5.setPrice(24.99);
                p5.setDescription("Perfect casual t-shirt for daily wear.");
                p5.setImageUrl("https://images.unsplash.com/photo-1521572215715-e5e73b2a20a7?w=500");
                p5.setStockQuantity(60);

                Product p6 = new Product();
                p6.setName("Wool Blazer");
                p6.setBrand("FormalWear");
                p6.setCategory("Jackets");
                p6.setPrice(149.99);
                p6.setDescription("Sophisticated wool blazer for formal occasions.");
                p6.setImageUrl("https://images.unsplash.com/photo-1570040567192-e75c85785c60?w=500");
                p6.setStockQuantity(20);

                Product p7 = new Product();
                p7.setName("Classic Belt");
                p7.setBrand("AccessoryCo");
                p7.setCategory("Accessories");
                p7.setPrice(34.99);
                p7.setDescription("Timeless leather belt that goes with everything.");
                p7.setImageUrl("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500");
                p7.setStockQuantity(45);

                Product p8 = new Product();
                p8.setName("Designer Watch");
                p8.setBrand("TimeKeeper");
                p8.setCategory("Accessories");
                p8.setPrice(199.99);
                p8.setDescription("Elegant designer watch for the modern man.");
                p8.setImageUrl("https://images.unsplash.com/photo-1523293182086-7651a899d37f?w=500");
                p8.setStockQuantity(15);

                Product p9 = new Product();
                p9.setName("Distressed Jeans");
                p9.setBrand("EdgeDenim");
                p9.setCategory("Jeans");
                p9.setPrice(59.99);
                p9.setDescription("Trendy distressed jeans with style.");
                p9.setImageUrl("https://images.unsplash.com/photo-1516992654410-a1fb8feb6c38?w=500");
                p9.setStockQuantity(30);

                Product p10 = new Product();
                p10.setName("Oxford Shoes");
                p10.setBrand("ClassicStride");
                p10.setCategory("Shoes");
                p10.setPrice(119.99);
                p10.setDescription("Traditional oxford shoes for formal wear.");
                p10.setImageUrl("https://images.unsplash.com/photo-1543163521-9733539c54dd?w=500");
                p10.setStockQuantity(22);

                productRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10));
            }
        };
    }}