package com.example.mensfashionstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded product images from uploads/products folder
        String uploadPath = Paths.get("uploads/products").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(uploadPath);
    }
}
