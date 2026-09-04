package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.ExamResultDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamResultDetailRepository extends JpaRepository<ExamResultDetail, Long> {
    List<ExamResultDetail> findByExamResultId(Long examResultId);
}
