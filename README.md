# Quiz Application (Ứng dụng Trắc nghiệm Online)

Hệ thống ứng dụng trắc nghiệm trực tuyến được xây dựng trên nền tảng **Spring Boot** kết hợp với **Thymeleaf**, **Spring Security**, và **MySQL**.

---

## 🛠️ Công nghệ sử dụng
- **Java**: Version 25 (hoặc Java 17+)
- **Framework**: Spring Boot 4.0.7 / Spring Framework
- **Security**: Spring Security, BCrypt Password Encoder, JWT Token
- **Database**: MySQL 8.0+
- **ORM**: Spring Data JPA / Hibernate
- **Template Engine**: Thymeleaf
- **Build Tool**: Gradle
- **Bootstrap**: Bootstrap 5.3.3
- **Link presentation**: https://canva.link/s46tp3drs5vj1az
---

## 📁 Cấu trúc Thư mục Dự án

```text
src/main/java/com/codegym/Quiz/
├── authentication/             # Package quản lý xác thực & JWT
│   ├── controller/             # Controller xác thực (Web & REST API)
│   ├── security/               # Filter & Config cho JWT / Spring Security
│   ├── service/                # Business logic cho Đăng ký / Đăng nhập
│   └── util/                   # Utility cho JWT token generation & parse
├── config/                     # Các lớp Cấu hình (SecurityConfig, PasswordConfig)
├── controller/                 # Web Controllers (Admin, User, Home)
├── dto/                        # Data Transfer Objects (Login, Register, Password...)
├── entity/                     # JPA Entities (User, Role, ...)
├── repository/                 # Spring Data JPA Repositories
└── service/                    # Business Logic Services (UserService, EmailService...)
```

---

## 🚀 Hướng dẫn Cài đặt & Chạy Dự án

### 1. Chuẩn bị Cơ sở Dữ liệu (MySQL)
- Mở MySQL Client hoặc MySQL Workbench và tạo database:
  ```sql
  CREATE DATABASE quiz_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```
- Hoặc import trực tiếp file SQL có sẵn trong dự án: `src/main/resources/sql/quiz_db.sql`.

### 2. Cấu hình ứng dụng
Chỉnh sửa thông tin kết nối DB trong file `src/main/resources/application.properties` :
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/quiz_db?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
spring.datasource.username=DB_USERNAME
spring.datasource.password=DB_PASSWORD
```

### 3. Chạy ứng dụng
Mở Terminal tại thư mục gốc của dự án và chạy bằng Gradle Wrapper:

**Trên Windows (PowerShell/CMD):**
```powershell
.\gradlew bootRun
```

**Trên Linux/macOS:**
```bash
./gradlew bootRun
```

Ứng dụng sẽ chạy tại địa chỉ: `http://localhost:8080`

