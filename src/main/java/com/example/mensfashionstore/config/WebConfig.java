package com.example.mensfashionstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:uploads/products}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        Path uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path uploadsRoot = uploadDirectory.getParent() != null ? uploadDirectory.getParent() : uploadDirectory;
        String uploadPath = uploadsRoot.toUri().toString();
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath)
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}
