package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** Phân trang danh sách câu hỏi toàn hệ thống */
    Page<Question> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Phân trang câu hỏi của 1 giáo viên */
    Page<Question> findByCreatedByOrderByCreatedAtDesc(
            User createdBy,
            Pageable pageable
    );

    /** Phân trang câu hỏi theo danh mục */
    Page<Question> findByCategoryIdOrderByCreatedAtDesc(
            Long categoryId,
            Pageable pageable
    );

    /** Phân trang câu hỏi theo danh mục và giáo viên */
    Page<Question> findByCategoryIdAndCreatedByOrderByCreatedAtDesc(
            Long categoryId,
            User createdBy,
            Pageable pageable
    );

    /**
     * Tìm kiếm câu hỏi toàn hệ thống
     * theo từ khóa trong nội dung câu hỏi.
     */
    @Query("""
            SELECT q FROM Question q
            WHERE LOWER(q.content)
                  LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY q.createdAt DESC
            """)
    Page<Question> searchAllByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * Tìm kiếm câu hỏi nâng cao toàn hệ thống:
     * keyword + category + difficulty.
     */
    @Query("""
            SELECT q FROM Question q
            WHERE (:keyword IS NULL
                   OR LOWER(q.content)
                      LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL
                   OR q.category.id = :categoryId)
              AND (:difficulty IS NULL
                   OR q.difficultyLevel = :difficulty)
            ORDER BY q.createdAt DESC
            """)
    Page<Question> searchAdvanced(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("difficulty") Question.DifficultyLevel difficulty,
            Pageable pageable
    );

    /**
     * Teacher tìm kiếm câu hỏi của chính mình
     * theo keyword, category và difficulty.
     */
    @Query("""
            SELECT q FROM Question q
            WHERE q.createdBy = :user
              AND (:keyword IS NULL
                   OR LOWER(q.content)
                      LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL
                   OR q.category.id = :categoryId)
              AND (:difficulty IS NULL
                   OR q.difficultyLevel = :difficulty)
            ORDER BY q.createdAt DESC
            """)
    Page<Question> searchAdvancedByUser(
            @Param("user") User user,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("difficulty") Question.DifficultyLevel difficulty,
            Pageable pageable
    );

    /** Kiểm tra xem danh mục có đang chứa câu hỏi hay không */
    boolean existsByCategoryId(Long categoryId);

    /**
     * Lấy các câu hỏi không nằm trong danh sách id.
     * Dùng khi chọn thêm câu hỏi cho Exam.
     */
    List<Question> findByIdNotIn(List<Long> ids);

    /**
     * Lấy toàn bộ câu hỏi thuộc một category.
     */
    List<Question> findByCategoryId(Long categoryId);
}