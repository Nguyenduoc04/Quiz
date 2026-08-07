package com.codegym.Quiz.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.codegym.Quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm người dùng bằng Username
    Optional<User> findByUsername(String username);

    // Tìm người dùng bằng Email
    Optional<User> findByEmail(String email);

    // Kiểm tra Username đã tồn tại hay chưa
    boolean existsByUsername(String username);

    // Kiểm tra Email đã tồn tại hay chưa
    boolean existsByEmail(String email);

    // 1. Kiểm tra Email đã được dùng bởi TÀI KHOẢN KHÁC hay chưa (Phục vụ Cập nhật Profile)
    boolean existsByEmailAndIdNot(String email, Long id);

    // 2. Tìm người dùng bằng mã OTP (Phục vụ tính năng Quên / Đặt lại mật khẩu)
    Optional<User> findByResetOtp(String resetOtp);
    // Lấy danh sách học viên có phân trang
    Page<User> findByRoles_Name(String roleName, Pageable pageable);

    // Đếm tổng số học viên
    long countByRoles_Name(String roleName);
    // Tìm kiếm theo Username, Full Name hoặc Email
    @Query("""
            SELECT u
            FROM User u
            JOIN u.roles r
            WHERE r.name = :role
            AND (
                LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    Page<User> searchUsers(
            @Param("role") String role,
            @Param("keyword") String keyword,
            Pageable pageable
    );

}