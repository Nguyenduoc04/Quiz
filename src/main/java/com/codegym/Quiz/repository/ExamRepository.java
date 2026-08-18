package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamStatus;
import com.codegym.Quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByStatus(ExamStatus status);
    List<Exam> findByCreatedBy(String createdBy);
    List<Question> findByIdNotIn(List<Long> ids);
}