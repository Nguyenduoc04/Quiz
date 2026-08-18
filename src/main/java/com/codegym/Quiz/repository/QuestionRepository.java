package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCategoryId(Long categoryId);
}