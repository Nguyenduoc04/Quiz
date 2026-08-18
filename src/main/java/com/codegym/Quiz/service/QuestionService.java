package com.codegym.Quiz.service;

import com.codegym.Quiz.entity.Question;
import java.util.List;

public interface QuestionService {
    List<Question> getAllQuestions();
    List<Question> getQuestionsByCategoryId(Long categoryId);
}