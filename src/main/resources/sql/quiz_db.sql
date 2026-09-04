-- ============================================================
--  Quiz App - Script khởi tạo Database (Tự động reset bảng nếu đã tồn tại)
--  Cập nhật dữ liệu phong phú: 2026-09-04
-- ============================================================

-- 1. Tạo & Chọn Database
CREATE DATABASE IF NOT EXISTS quiz_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE quiz_db;

-- Tắt kiểm tra khóa ngoại để Drop các bảng cũ an toàn
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS exam_result_details;
DROP TABLE IF EXISTS exam_results;
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
-- 5. Tạo bảng categories
-- ============================================================
CREATE TABLE categories (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    created_by  BIGINT,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- 6. Tạo bảng questions
-- ============================================================
CREATE TABLE questions (
    id               BIGINT      AUTO_INCREMENT PRIMARY KEY,
    content          TEXT        NOT NULL,
    difficulty_level VARCHAR(20) DEFAULT 'MEDIUM',
    question_type    VARCHAR(20) DEFAULT 'SINGLE_CHOICE',
    score            DOUBLE      DEFAULT 1.0,
    explanation      TEXT,
    category_id      BIGINT,
    created_by       BIGINT,
    created_at       DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- 7. Tạo bảng answers
-- ============================================================
CREATE TABLE answers (
    id            BIGINT   AUTO_INCREMENT PRIMARY KEY,
    content       TEXT     NOT NULL,
    is_correct    BOOLEAN  NOT NULL DEFAULT FALSE,
    display_order INT      DEFAULT 1,
    question_id   BIGINT   NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- ============================================================
-- 8. Tạo bảng exams
-- ============================================================
CREATE TABLE exams (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    description      TEXT,
    duration_minutes INT          DEFAULT 60,
    total_score      DOUBLE       DEFAULT 10.0,
    passing_score    DOUBLE       DEFAULT 5.0,
    status           VARCHAR(20)  DEFAULT 'DRAFT',
    created_by       BIGINT,
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- 9. Tạo bảng exam_questions
-- ============================================================
CREATE TABLE exam_questions (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id        BIGINT NOT NULL,
    question_id    BIGINT NOT NULL,
    question_order INT    DEFAULT 1,
    custom_score   DOUBLE NULL,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- ============================================================
-- 10. Tạo bảng exam_results
-- ============================================================
CREATE TABLE exam_results (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NULL,
    exam_id         BIGINT NOT NULL,
    score           DOUBLE   DEFAULT 0.0,
    total_score     DOUBLE   DEFAULT 10.0,
    correct_count   INT      DEFAULT 0,
    total_questions INT      DEFAULT 0,
    is_passed       BOOLEAN  DEFAULT FALSE,
    submitted_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
);

-- ============================================================
-- 11. Tạo bảng exam_result_details
-- ============================================================
CREATE TABLE exam_result_details (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_result_id     BIGINT NOT NULL,
    question_id        BIGINT NOT NULL,
    selected_answer_id BIGINT NULL,
    is_correct         BOOLEAN DEFAULT FALSE,
    score_obtained     DOUBLE  DEFAULT 0.0,
    FOREIGN KEY (exam_result_id) REFERENCES exam_results(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id)    REFERENCES questions(id)    ON DELETE CASCADE
);

-- ============================================================
-- 12. Dữ liệu mẫu: Roles
-- ============================================================
INSERT INTO roles (id, name) VALUES
    (1, 'ROLE_ADMIN'),           -- Quản trị viên
    (2, 'ROLE_STUDENT'),         -- Học viên
    (3, 'ROLE_TEACHER'),         -- Giáo viên
    (4, 'ROLE_PENDING_TEACHER'); -- Chờ duyệt giáo viên

-- ============================================================
-- 13. Dữ liệu mẫu: Users (Mật khẩu gốc cho tất cả: 123456)
-- ============================================================
SET @pw = '$2a$10$XvxijM6iKkADF83jqQRz0.CLsx3KD5yD1KJkz7/IOoSZutbrt9B2W';

INSERT INTO users (id, username, password, email, full_name, enabled, created_at) VALUES
    (1, 'admin',    @pw, 'admin@quiz.com',    'Administrator', TRUE, NOW()),
    (2, 'student', @pw, 'student1@quiz.com', 'Nguyễn Văn An', TRUE, NOW()),
    (3, 'teacher', @pw, 'teacher1@quiz.com', 'Trần Thị Bình', TRUE, NOW());

-- ============================================================
-- 14. Gán quyền mẫu
-- ============================================================
INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 1),   -- admin    -> ROLE_ADMIN
    (2, 2),   -- student1 -> ROLE_STUDENT
    (3, 3);   -- teacher1 -> ROLE_TEACHER

-- ============================================================
-- 15. Dữ liệu mẫu: Categories
-- ============================================================
INSERT INTO categories (id, name, description, created_by, created_at, updated_at) VALUES
    (1, 'Java Core', 'Các kiến thức nền tảng về Lập trình Java OOP, Collection, Exception, Multithreading.', 3, NOW(), NOW()),
    (2, 'Spring Boot', 'Framework Spring Boot, Spring MVC, Spring Data JPA, Spring Security RESTful API.', 3, NOW(), NOW()),
    (3, 'MySQL Database', 'Cơ sở dữ liệu quan hệ MySQL, câu lệnh SQL, Join, Indexing & Optimization.', 3, NOW(), NOW()),
    (4, 'Web Frontend', 'Kiến thức xây dựng giao diện với HTML5, CSS3, JavaScript ES6+ và Web Responsive.', 3, NOW(), NOW());

-- ============================================================
-- 16. Dữ liệu mẫu: Questions (16 Câu hỏi mẫu chất lượng)
-- ============================================================
INSERT INTO questions (id, content, difficulty_level, question_type, score, explanation, category_id, created_by, created_at, updated_at) VALUES
    -- Java Core (1-5)
    (1, 'Kích thước của kiểu dữ liệu int trong Java là bao nhiêu bit?', 'EASY', 'SINGLE_CHOICE', 2.0, 'Trong Java, kiểu int luôn cố định 32-bit (4 bytes).', 1, 3, NOW(), NOW()),
    (2, 'Phương thức nào dùng để kích hoạt một Thread trong Java?', 'MEDIUM', 'SINGLE_CHOICE', 2.0, 'Gọi phương thức start() để hệ điều hành tạo stack mới cho Thread.', 1, 3, NOW(), NOW()),
    (3, 'Interface nào là gốc (root) của Collection Hierarchy trong Java?', 'EASY', 'SINGLE_CHOICE', 2.0, 'Collection interface là giao diện gốc của List, Set, Queue.', 1, 3, NOW(), NOW()),
    (4, 'Từ khóa nào được dùng để khai báo một hằng số không thể thay đổi giá trị trong Java?', 'EASY', 'SINGLE_CHOICE', 2.0, 'Từ khóa final biến giá trị tham chiếu hoặc biến primitive thành không thể sửa đổi.', 1, 3, NOW(), NOW()),
    (5, 'Trong Java, lớp nào cho phép nối chuỗi hiệu năng cao và an toàn luồng (Thread-safe)?', 'MEDIUM', 'SINGLE_CHOICE', 2.0, 'StringBuffer được synchronized nên an toàn luồng hơn StringBuilder.', 1, 3, NOW(), NOW()),

    -- Spring Boot (6-9)
    (6, 'Annotation nào được sử dụng để đánh dấu một lớp là Spring REST Controller?', 'EASY', 'SINGLE_CHOICE', 2.5, '@RestController = @Controller + @ResponseBody.', 2, 3, NOW(), NOW()),
    (7, 'Annotation nào dùng để inject dependency tự động trong Spring Framework?', 'EASY', 'SINGLE_CHOICE', 2.5, '@Autowired tự động tiêm bean tương ứng vào attribute/constructor.', 2, 3, NOW(), NOW()),
    (8, 'Phương thức HTTP nào được khuyến nghị sử dụng khi tạo mới một tài nguyên (Resource)?', 'EASY', 'SINGLE_CHOICE', 2.5, 'POST được chuẩn hóa cho hành động tạo mới tài nguyên trên REST API.', 2, 3, NOW(), NOW()),
    (9, 'File cấu hình mặc định dạng YAML của Spring Boot thường tên là gì?', 'EASY', 'SINGLE_CHOICE', 2.5, 'Spring Boot mặc định đọc file application.yml hoặc application.properties.', 2, 3, NOW(), NOW()),

    -- MySQL Database (10-13)
    (10, 'Mệnh đề SQL nào dùng để lọc kết quả sau khi đã áp dụng GROUP BY?', 'MEDIUM', 'SINGLE_CHOICE', 2.5, 'WHERE dùng trước GROUP BY, HAVING dùng sau GROUP BY để lọc các nhóm.', 3, 3, NOW(), NOW()),
    (11, 'Loại JOIN nào trả về tất cả các hàng từ bảng bên trái và các hàng phù hợp từ bảng bên phải?', 'EASY', 'SINGLE_CHOICE', 2.5, 'LEFT JOIN giữ nguyên toàn bộ bản ghi ở bảng bên trái.', 3, 3, NOW(), NOW()),
    (12, 'Từ khóa SQL nào dùng để loại bỏ các dòng trùng lặp trong kết quả SELECT?', 'EASY', 'SINGLE_CHOICE', 2.5, 'DISTINCT lọc các bộ giá trị trùng nhau.', 3, 3, NOW(), NOW()),
    (13, 'Chỉ mục (Index) trong MySQL giúp cải thiện yếu tố nào của cơ sở dữ liệu?', 'MEDIUM', 'SINGLE_CHOICE', 2.5, 'Index giúp tăng tốc truy vấn tìm kiếm dữ liệu (SELECT).', 3, 3, NOW(), NOW()),

    -- Web Frontend (14-16)
    (14, 'Thẻ HTML5 nào được sử dụng để nhúng một tệp âm thanh vào trang web?', 'EASY', 'SINGLE_CHOICE', 2.5, 'Thẻ <audio> là chuẩn HTML5 để phát nhạc/âm thanh.', 4, 3, NOW(), NOW()),
    (15, 'Trong CSS, thuộc tính nào dùng để điều chỉnh khoảng cách giữa nội dung và đường viền (border)?', 'EASY', 'SINGLE_CHOICE', 2.5, 'Padding điều chỉnh khoảng cách trong, margin điều chỉnh khoảng cách ngoài.', 4, 3, NOW(), NOW()),
    (16, 'Trong JavaScript (ES6), từ khóa nào dùng để khai báo biến có phạm vi block-scope và không thể gán lại?', 'EASY', 'SINGLE_CHOICE', 2.5, 'const dùng cho biến hằng số có hằng số tham chiếu.', 4, 3, NOW(), NOW());

-- ============================================================
-- 17. Dữ liệu mẫu: Answers
-- ============================================================
INSERT INTO answers (id, content, is_correct, display_order, question_id) VALUES
    -- Q1
    (1, '16 bit', FALSE, 1, 1),
    (2, '32 bit', TRUE,  2, 1),
    (3, '64 bit', FALSE, 3, 1),
    (4, '8 bit',  FALSE, 4, 1),
    
    -- Q2
    (5, 'run()',     FALSE, 1, 2),
    (6, 'start()',   TRUE,  2, 2),
    (7, 'init()',    FALSE, 3, 2),
    (8, 'execute()', FALSE, 4, 2),
    
    -- Q3
    (9,  'List',       FALSE, 1, 3),
    (10, 'Collection', TRUE,  2, 3),
    (11, 'Set',        FALSE, 3, 3),
    (12, 'Map',        FALSE, 4, 3),

    -- Q4
    (13, 'static',   FALSE, 1, 4),
    (14, 'final',    TRUE,  2, 4),
    (15, 'const',    FALSE, 3, 4),
    (16, 'volatile', FALSE, 4, 4),

    -- Q5
    (17, 'String',        FALSE, 1, 5),
    (18, 'StringBuilder', FALSE, 2, 5),
    (19, 'StringBuffer',  TRUE,  3, 5),
    (20, 'StringJoiner',  FALSE, 4, 5),

    -- Q6
    (21, '@Controller',     FALSE, 1, 6),
    (22, '@RestController', TRUE,  2, 6),
    (23, '@Service',        FALSE, 3, 6),
    (24, '@Component',      FALSE, 4, 6),

    -- Q7
    (25, '@Inject',     FALSE, 1, 7),
    (26, '@Autowired', TRUE,  2, 7),
    (27, '@Resource',   FALSE, 3, 7),
    (28, '@Bean',       FALSE, 4, 7),

    -- Q8
    (29, 'GET',    FALSE, 1, 8),
    (30, 'POST',   TRUE,  2, 8),
    (31, 'PUT',    FALSE, 3, 8),
    (32, 'DELETE', FALSE, 4, 8),

    -- Q9
    (33, 'config.yml',      FALSE, 1, 9),
    (34, 'application.yml', TRUE,  2, 9),
    (35, 'spring.yml',      FALSE, 3, 9),
    (36, 'bootstrap.yml',   FALSE, 4, 9),

    -- Q10
    (37, 'WHERE',    FALSE, 1, 10),
    (38, 'HAVING',   TRUE,  2, 10),
    (39, 'ORDER BY', FALSE, 3, 10),
    (40, 'LIMIT',    FALSE, 4, 10),

    -- Q11
    (41, 'INNER JOIN', FALSE, 1, 11),
    (42, 'LEFT JOIN',  TRUE,  2, 11),
    (43, 'RIGHT JOIN', FALSE, 3, 11),
    (44, 'FULL JOIN',  FALSE, 4, 11),

    -- Q12
    (45, 'UNIQUE',   FALSE, 1, 12),
    (46, 'DISTINCT', TRUE,  2, 12),
    (47, 'FILTER',   FALSE, 3, 12),
    (48, 'GROUP',    FALSE, 4, 12),

    -- Q13
    (49, 'Tốc độ ghi dữ liệu (INSERT)',      FALSE, 1, 13),
    (50, 'Tốc độ truy vấn dữ liệu (SELECT)', TRUE,  2, 13),
    (51, 'Bảo mật dữ liệu',                  FALSE, 3, 13),
    (52, 'Tiết kiệm dung lượng đĩa cứng',   FALSE, 4, 13),

    -- Q14
    (53, '<sound>', FALSE, 1, 14),
    (54, '<audio>', TRUE,  2, 14),
    (55, '<music>', FALSE, 3, 14),
    (56, '<media>', FALSE, 4, 14),

    -- Q15
    (57, 'margin',  FALSE, 1, 15),
    (58, 'padding', TRUE,  2, 15),
    (59, 'spacing', FALSE, 3, 15),
    (60, 'border',  FALSE, 4, 15),

    -- Q16
    (61, 'var',   FALSE, 1, 16),
    (62, 'const', TRUE,  2, 16),
    (63, 'let',   FALSE, 3, 16),
    (64, 'def',   FALSE, 4, 16);

-- ============================================================
-- 18. Dữ liệu mẫu: Exams & ExamQuestions (Đa dạng 6 bài thi cho các danh mục)
-- ============================================================
INSERT INTO exams (id, title, description, duration_minutes, total_score, passing_score, status, created_by, created_at, updated_at) VALUES
    (1, 'Đề Thi Khảo Sát Java Core & OOP 2026', 'Đề thi tổng hợp kiến thức Java căn bản đến nâng cao. Thời gian 30 phút.', 30, 10.0, 5.0, 'PUBLISHED', 3, NOW(), NOW()),
    (2, 'Luyện tập Collection Framework & Multithreading', 'Kiểm tra chuyên sâu về Java Collection và Xử lý đa luồng.', 45, 10.0, 6.0, 'PUBLISHED', 3, NOW(), NOW()),
    (3, 'Spring Data JPA & Security RESTful API', 'Bài thi kiểm tra kiến thức về Spring Boot RESTful API và Security.', 60, 10.0, 5.0, 'PUBLISHED', 3, NOW(), NOW()),
    (4, 'Tối ưu hóa Truy vấn MySQL Indexing & Join', 'Đề thi thực hành truy vấn SQL nâng cao và tối ưu hóa MySQL.', 30, 10.0, 5.0, 'PUBLISHED', 3, NOW(), NOW()),
    (5, 'HTML5, CSS3 & JavaScript ES6+ Cơ bản', 'Bài thi đánh giá kiến thức lập trình Web Frontend căn bản.', 45, 10.0, 5.0, 'PUBLISHED', 3, NOW(), NOW()),
    (6, 'Spring Boot Core & Dependency Injection', 'Bài thi kiểm tra khái niệm Inversion of Control và Beans trong Spring.', 30, 10.0, 5.0, 'PUBLISHED', 3, NOW(), NOW());

INSERT INTO exam_questions (exam_id, question_id, question_order) VALUES
    -- Exam 1 (Java Core)
    (1, 1, 1),
    (1, 2, 2),
    (1, 3, 3),
    (1, 4, 4),
    (1, 5, 5),

    -- Exam 2 (Java Core)
    (2, 2, 1),
    (2, 3, 2),
    (2, 5, 3),

    -- Exam 3 (Spring Boot)
    (3, 6, 1),
    (3, 7, 2),
    (3, 8, 3),
    (3, 9, 4),

    -- Exam 4 (MySQL Database)
    (4, 10, 1),
    (4, 11, 2),
    (4, 12, 3),
    (4, 13, 4),

    -- Exam 5 (Web Frontend)
    (5, 14, 1),
    (5, 15, 2),
    (5, 16, 3),

    -- Exam 6 (Spring Boot)
    (6, 6, 1),
    (6, 7, 2);