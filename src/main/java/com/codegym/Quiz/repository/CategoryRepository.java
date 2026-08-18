package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Tìm kiếm danh mục theo tên
    Optional<Category> findByName(String name);

    // Kiểm tra tên danh mục đã tồn tại chưa (dùng khi tạo mới / chỉnh sửa)
    boolean existsByName(String name);
}