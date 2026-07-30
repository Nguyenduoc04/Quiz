package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Tìm Role theo tên (VD: "ROLE_STUDENT", "ROLE_ADMIN")
    Optional<Role> findByName(String name);
}
