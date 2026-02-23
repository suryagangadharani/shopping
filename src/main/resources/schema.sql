-- Sample schema for mens_store
CREATE DATABASE IF NOT EXISTS mens_store;
USE mens_store;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(255),
  email VARCHAR(255) UNIQUE,
  password VARCHAR(255),
  role VARCHAR(50),
  is_active BOOLEAN DEFAULT true,
  registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_login TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255),
  brand VARCHAR(255),
  category VARCHAR(100),
  price DOUBLE,
  description TEXT,
  image_url VARCHAR(512),
  stock_quantity INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  total_amount DOUBLE,
  order_status VARCHAR(50) DEFAULT 'PENDING',
  CONSTRAINT FKpktxwhj3x1syfziyhchukjma9 FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT,
  price DOUBLE,
  CONSTRAINT FKocimc7dtr037rh4ls4l95nlfi FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT FKco6i0bx53t8m98jkkbhvnfex FOREIGN KEY (order_id) REFERENCES orders (id)
);

-- Example insert for admin (replace hashed_password with bcrypt hash)
-- INSERT INTO users (full_name, email, password, role) VALUES ('Site Admin','admin@store.com','$2a$10$hashed_password', 'ADMIN');
