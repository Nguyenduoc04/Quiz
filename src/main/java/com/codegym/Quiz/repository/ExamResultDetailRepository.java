package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.ExamResultDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamResultDetailRepository
        extends JpaRepository<ExamResultDetail, Long> {

    List<ExamResultDetail> findByExamResult_Id(Long examResultId);
}