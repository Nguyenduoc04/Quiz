package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.Category;
import com.codegym.Quiz.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Kiểm tra tên danh mục đã tồn tại chưa (toàn hệ thống) */
    boolean existsByName(String name);

    /** Kiểm tra tên danh mục đã tồn tại chưa, ngoại trừ id hiện tại (dùng khi update) */
    boolean existsByNameAndIdNot(String name, Long id);

    /** Lấy danh mục theo người tạo, có phân trang */
    Page<Category> findByCreatedBy(User createdBy, Pageable pageable);

    /** Lấy tất cả danh mục theo người tạo (không phân trang) */
    List<Category> findByCreatedBy(User createdBy);

    /** Tìm kiếm danh mục theo tên hoặc mô tả (Admin – toàn hệ thống) */
    @Query("""
            SELECT c FROM Category c
            WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY c.createdAt DESC
            """)
    Page<Category> searchAll(@Param("keyword") String keyword, Pageable pageable);

    /** Tìm kiếm danh mục của 1 giáo viên cụ thể theo tên hoặc mô tả */
    @Query("""
            SELECT c FROM Category c
            WHERE c.createdBy = :user
              AND (
                  LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY c.createdAt DESC
            """)
    Page<Category> searchByUser(@Param("user") User user,
                                @Param("keyword") String keyword,
                                Pageable pageable);

    /** Lấy tất cả danh mục, sắp xếp theo thời gian tạo mới nhất */
    Page<Category> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Đếm số câu hỏi trong 1 danh mục */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.category.id = :categoryId")
    long countQuestionsByCategoryId(@Param("categoryId") Long categoryId);

    /** Tìm danh mục theo tên chính xác */
    Optional<Category> findByName(String name);
}
