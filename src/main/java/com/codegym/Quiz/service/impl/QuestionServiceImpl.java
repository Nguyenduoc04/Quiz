package com.codegym.Quiz.service.impl;

import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.repository.QuestionRepository;
import com.codegym.Quiz.service.QuestionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    @Override
    public List<Question> getQuestionsByCategoryId(Long categoryId) {
        return questionRepository.findByCategoryId(categoryId);
    }
}