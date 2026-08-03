package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}