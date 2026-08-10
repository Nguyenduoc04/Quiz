package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    List<ExamQuestion> findByExamIdOrderByQuestionOrderAsc(Long examId);
    void deleteByExamId(Long examId);
}