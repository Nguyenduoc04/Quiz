package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    List<ExamQuestion> findByExamIdOrderByQuestionOrderAsc(Long examId);

    boolean existsByExamIdAndQuestionId(Long examId, Long questionId);

    @Query("SELECT MAX(eq.questionOrder) FROM ExamQuestion eq WHERE eq.exam.id = :examId")
    Optional<Integer> findMaxOrderNumByExamId(@Param("examId") Long examId);

    void deleteByExamIdAndQuestionId(Long examId, Long questionId);

    void deleteByExamId(Long examId);
}