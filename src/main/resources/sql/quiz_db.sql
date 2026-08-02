-- ============================================================
--  Quiz App - Script khởi tạo Database
--  Cập nhật: 2026-07-30
--
--  Hướng dẫn sử dụng cho developer mới:
--  1. Mở MySQL Workbench (hoặc MySQL Shell)
--  2. Chạy toàn bộ file này một lần duy nhất
--  3. Cấu hình kết nối trong application.properties:
--       spring.datasource.url=jdbc:mysql://localhost:3306/quiz_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
--       spring.datasource.username=root
--       spring.datasource.password=<mật khẩu MySQL của bạn>
--  4. Chạy app: ./gradlew bootRun
--  5. Truy cập: http://localhost:8080
--
--  Tài khoản mẫu (mật khẩu đều là: 123456):
--    admin      -> ROLE_ADMIN   (quản trị viên)
--    trang_dev  -> ROLE_USER    (người dùng thường)
--    guest      -> ROLE_USER    (người dùng thường)
-- ============================================================

-- 0. Xóa database cũ nếu muốn làm sạch dữ liệu từ đầu
DROP DATABASE IF EXISTS quiz_db;

-- 1. Tạo Database mới
CREATE DATABASE IF NOT EXISTS quiz_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE quiz_db;

-- ============================================================
-- 2. Tạo bảng roles
-- ============================================================
CREATE TABLE IF NOT EXISTS roles (
    id   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50)  NOT NULL UNIQUE
    );

-- ============================================================
-- 3. Tạo bảng users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username  VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,           -- BCrypt hash
    email     VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100),
    enabled   BOOLEAN      NOT NULL DEFAULT TRUE,
    reset_otp  VARCHAR(10)  DEFAULT NULL,      -- Mã OTP quên mật khẩu
    otp_expiry DATETIME     DEFAULT NULL       -- Thời hạn OTP
    );

-- ============================================================
-- 4. Tạo bảng liên kết user_roles (nhiều-nhiều)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id)  ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id)  ON DELETE CASCADE
    );

-- ============================================================
-- 5. Dữ liệu mẫu: Roles
-- ============================================================
INSERT IGNORE INTO roles (id, name) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN'),
(3, 'ROLE_STUDENT');

-- ============================================================
-- 6. Dữ liệu mẫu: Users
--    Mật khẩu gốc: 123456
-- ============================================================
SET @pw = '$2a$10$XvxijM6iKkADF83jqQRz0.CLsx3KD5yD1KJkz7/IOoSZutbrt9B2W';

INSERT IGNORE INTO users (id, username, password, email, full_name, enabled) VALUES
(1, 'admin', @pw, 'admin@quiz.com', 'Administrator', TRUE),
(2, 'trang_dev', @pw, 'trangnguyen@gmail.com', 'Nguyễn Trang', TRUE),
(3, 'guest', @pw, 'module5codegym@gmail.com', 'Guest User', TRUE);

-- ============================================================
-- 7. Gán quyền cho từng user
-- ============================================================
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
(1, 2),   -- admin     -> ROLE_ADMIN
(2, 1),   -- trang_dev -> ROLE_USER
(3, 1);   -- guest     -> ROLE_USER

-- ============================================================
-- 8. Kiểm tra kết quả
-- ============================================================
SELECT
    u.id,
    u.username,
    u.email,
    u.full_name,
    u.enabled,
    u.reset_otp,
    u.otp_expiry,
    r.name AS role
FROM users u
         JOIN user_roles ur ON u.id = ur.user_id
         JOIN roles r       ON ur.role_id = r.id
ORDER BY u.id;