-- ============================================================
--  Quiz App - Script khởi tạo Database
--  Cập nhật: 2026-08-02
--
--  Hướng dẫn sử dụng cho developer mới:
--  1. Mở MySQL Workbench (hoặc MySQL Shell)
--  2. Chạy toàn bộ file này một lần duy nhất
--  3. Cấu hình kết nối trong application.properties:
--       spring.datasource.url=jdbc:mysql://localhost:3306/quiz_db?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
--       spring.datasource.username=root
--       spring.datasource.password=<mật khẩu MySQL của bạn>
--  4. Chạy app: ./gradlew bootRun
--  5. Truy cập: http://localhost:8080
--
--  Tài khoản mẫu (mật khẩu đều là: 123456):
--    admin      -> ROLE_ADMIN   (quản trị viên – toàn quyền)
--    student1   -> ROLE_STUDENT (học viên – lưu kết quả, vào lớp riêng)
--    user1      -> ROLE_USER    (người dùng thường – quiz public, không lưu kết quả)
--
--  Phân quyền hệ thống:
--    ROLE_USER    : Làm quiz public | Kết quả KHÔNG lưu | Không vào lớp riêng
--    ROLE_STUDENT : Làm quiz public | Kết quả ĐƯỢC lưu  | Vào lớp riêng (nếu là thành viên)
--    ROLE_ADMIN   : Toàn quyền quản trị
--
--  Nâng cấp ROLE_USER -> ROLE_STUDENT:
--    Người dùng nhập mã lớp học -> Class Module xử lý -> thêm ROLE_STUDENT
-- ============================================================

-- 1. Tạo Database
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
    enabled   BOOLEAN      NOT NULL DEFAULT TRUE
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
    (1, 'ROLE_USER'),      -- Người dùng thường (mặc định khi đăng ký)
    (2, 'ROLE_ADMIN'),     -- Quản trị viên
    (3, 'ROLE_STUDENT');   -- Học viên (sau khi nhập mã lớp)

-- ============================================================
-- 6. Dữ liệu mẫu: Users
--    Mật khẩu gốc: 123456
--    Hash BCrypt (strength=10) tương thích Spring BCryptPasswordEncoder
-- ============================================================
SET @pw = '$2a$10$XvxijM6iKkADF83jqQRz0.CLsx3KD5yD1KJkz7/IOoSZutbrt9B2W';

INSERT IGNORE INTO users (id, username, password, email, full_name, enabled) VALUES
    (1, 'admin',    @pw, 'admin@quiz.com',      'Administrator',    TRUE),
    (2, 'student1', @pw, 'student1@quiz.com',   'Nguyễn Văn An',    TRUE),
    (3, 'user1',    @pw, 'user1@quiz.com',       'Trần Thị Bình',   TRUE);

-- ============================================================
-- 7. Gán quyền cho từng user
-- ============================================================
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
    (1, 2),   -- admin    -> ROLE_ADMIN
    (2, 3),   -- student1 -> ROLE_STUDENT
    (3, 1);   -- user1    -> ROLE_USER (thường)

-- ============================================================
-- 8. Kiểm tra kết quả
-- ============================================================
SELECT
    u.id,
    u.username,
    u.email,
    u.full_name,
    u.enabled,
    r.name AS role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r       ON ur.role_id = r.id
ORDER BY u.id;

-- ============================================================
-- 9. Bổ sung các bảng Quiz / Exam
-- ============================================================
CREATE TABLE IF NOT EXISTS exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS exam_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT NOT NULL,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);