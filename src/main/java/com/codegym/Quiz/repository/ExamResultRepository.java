package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    List<ExamResult> findByStudent_Id(Long studentId);

    List<ExamResult> findByExam_Id(Long examId);

    List<ExamResult> findByExam_IdAndStudent_Id(
            Long examId,
            Long studentId
    );
}