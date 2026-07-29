-- 1. Tạo Database quiz_db
CREATE DATABASE IF NOT EXISTS quiz_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quiz_db;

-- 2. Tạo bảng roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
    );

-- 3. Tạo bảng users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100),
    enabled BOOLEAN DEFAULT TRUE
    );

-- 4. Tạo bảng liên kết user_roles
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );

-- 5. Chèn dữ liệu mẫu Roles
INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_USER'), (2, 'ROLE_ADMIN');

-- 6. Chèn User mẫu (ID = 1L) để test chức năng /user/profile
INSERT IGNORE INTO users (id, username, password, email, full_name, enabled)
VALUES (1, 'trang_dev', '123456', 'trangnguyen@gmail.com', 'Nguyễn Trang', TRUE);

INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (1, 1);