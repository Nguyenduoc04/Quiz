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

    /** Phân trang danh sách bài thi theo người tạo (Giáo viên) */
    Page<Exam> findByCreatedByOrderByCreatedAtDesc(User createdBy, Pageable pageable);

    /** Phân trang danh sách bài thi công bố cho học sinh */
    Page<Exam> findByStatusOrderByCreatedAtDesc(ExamStatus status, Pageable pageable);

    List<Exam> findByStatus(ExamStatus status);

    List<Exam> findByCreatedBy(User createdBy);
}

