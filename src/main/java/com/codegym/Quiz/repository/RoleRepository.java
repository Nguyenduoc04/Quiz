package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Tìm kiếm Role theo tên (Phục vụ việc gán quyền mặc định ROLE_USER khi đăng ký)
    Optional<Role> findByName(String name);
}