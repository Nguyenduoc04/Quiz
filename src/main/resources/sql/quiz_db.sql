-- ============================================================
--  Quiz App - Script khởi tạo Database (Tự động reset bảng nếu đã tồn tại)
--  Cập nhật: 2026-08-19
-- ============================================================

-- 1. Tạo & Chọn Database
CREATE DATABASE IF NOT EXISTS quiz_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE quiz_db;

-- Tắt kiểm tra khóa ngoại để Drop các bảng cũ an toàn
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS exam_questions;
DROP TABLE IF EXISTS exams;
DROP TABLE IF EXISTS answers;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 2. Tạo bảng roles
-- ============================================================
CREATE TABLE roles (
    id   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50)  NOT NULL UNIQUE
);

-- ============================================================
-- 3. Tạo bảng users
-- ============================================================
CREATE TABLE users (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,           -- BCrypt hash
    email      VARCHAR(100) NOT NULL UNIQUE,
    full_name  VARCHAR(100),
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME     NULL
);

-- ============================================================
-- 4. Tạo bảng liên kết user_roles (nhiều-nhiều)
-- ============================================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- ============================================================
-- 5. Dữ liệu mẫu: Roles
-- ============================================================
INSERT INTO roles (id, name) VALUES
    (1, 'ROLE_ADMIN'),           -- Quản trị viên
    (2, 'ROLE_STUDENT'),         -- Học viên (mặc định khi đăng ký)
    (3, 'ROLE_TEACHER'),         -- Giáo viên
    (4, 'ROLE_PENDING_TEACHER'); -- Chờ duyệt giáo viên

-- ============================================================
-- 6. Dữ liệu mẫu: Users
--    Mật khẩu gốc cho tất cả: 123456
-- ============================================================
SET @pw = '$2a$10$XvxijM6iKkADF83jqQRz0.CLsx3KD5yD1KJkz7/IOoSZutbrt9B2W';

INSERT INTO users (id, username, password, email, full_name, enabled, created_at) VALUES
    (1, 'admin',    @pw, 'admin@quiz.com',    'Administrator', TRUE, NOW()),
    (2, 'student1', @pw, 'student1@quiz.com', 'Nguyễn Văn An', TRUE, NOW()),
    (3, 'teacher1', @pw, 'teacher1@quiz.com', 'Trần Thị Bình', TRUE, NOW());

-- ============================================================
-- 7. Gán quyền mẫu
-- ============================================================
INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 1),   -- admin    -> ROLE_ADMIN
    (2, 2),   -- student1 -> ROLE_STUDENT
    (3, 3);   -- teacher1 -> ROLE_TEACHER

-- ============================================================
-- 8. Kiểm tra kết quả khởi tạo
-- ============================================================
SELECT
    u.id,
    u.username,
    u.email,
    u.full_name,
    u.enabled,
    u.created_at,
    r.name AS role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r       ON ur.role_id = r.id
ORDER BY u.id;