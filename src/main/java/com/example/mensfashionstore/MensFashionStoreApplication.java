package com.example.mensfashionstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class MensFashionStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(MensFashionStoreApplication.class, args);
    }
}
