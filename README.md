# Mens Fashion Store

Spring Boot (Thymeleaf) based sample Men's Fashion Store.

Quick start:

1. Create MySQL database `mens_store` and update `src/main/resources/application.properties` with credentials.
2. Build and run:

```bash
mvn clean package
mvn spring-boot:run
```

Default admin created at startup: `admin@store.com` / `admin123` (change in production).

Features: products, product details, session cart, checkout, admin dashboard (add/delete products), registration and login.
