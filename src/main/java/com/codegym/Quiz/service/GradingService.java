package com.codegym.Quiz.service;

import com.codegym.Quiz.dto.SubmitAnswerDTO;
import com.codegym.Quiz.entity.ExamQuestion;

public interface GradingService {

    boolean isAnswerCorrect(
            ExamQuestion examQuestion,
            SubmitAnswerDTO submittedAnswer
    );

    double calculateScore(
            ExamQuestion examQuestion,
            SubmitAnswerDTO submittedAnswer
    );
}