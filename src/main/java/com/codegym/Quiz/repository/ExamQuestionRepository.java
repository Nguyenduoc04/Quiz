package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    @Query("SELECT eq FROM ExamQuestion eq WHERE eq.exam.id = :examId ORDER BY eq.question.id ASC")
    List<ExamQuestion> findByExamIdOrderByQuestionOrderAsc(@Param("examId") Long examId);

    void deleteByExamId(Long examId);
}