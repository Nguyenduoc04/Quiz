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
}