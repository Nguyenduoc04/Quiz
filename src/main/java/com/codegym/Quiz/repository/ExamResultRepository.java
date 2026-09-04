package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    List<ExamResult> findByUserIdOrderBySubmittedAtDesc(Long userId);
    List<ExamResult> findByExamIdOrderBySubmittedAtDesc(Long examId);
    List<ExamResult> findByExamIdOrderByScoreDescSubmittedAtAsc(Long examId);
}
