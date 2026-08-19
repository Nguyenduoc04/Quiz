package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamStatus;
import com.codegym.Quiz.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    /** Lấy Exam kèm danh sách câu hỏi (JOIN FETCH để tránh LazyInitializationException) */
    @org.springframework.data.jpa.repository.Query(
        "SELECT e FROM Exam e LEFT JOIN FETCH e.examQuestions eq LEFT JOIN FETCH eq.question WHERE e.id = :id")
    java.util.Optional<Exam> findByIdWithQuestions(@org.springframework.data.repository.query.Param("id") Long id);

    /** Phân trang danh sách bài thi theo người tạo (Giáo viên) */
    Page<Exam> findByCreatedByOrderByCreatedAtDesc(User createdBy, Pageable pageable);

    /** Phân trang danh sách bài thi công bố cho học sinh */
    Page<Exam> findByStatusOrderByCreatedAtDesc(ExamStatus status, Pageable pageable);

    List<Exam> findByStatus(ExamStatus status);

    List<Exam> findByCreatedBy(User createdBy);

    List<Exam> findByCreatedByUsername(String username);
}

