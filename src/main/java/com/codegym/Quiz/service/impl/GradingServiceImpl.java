package com.codegym.Quiz.service.impl;

import com.codegym.Quiz.dto.SubmitAnswerDTO;
import com.codegym.Quiz.entity.Answer;
import com.codegym.Quiz.entity.ExamQuestion;
import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.service.GradingService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GradingServiceImpl implements GradingService {

    @Override
    public boolean isAnswerCorrect(
            ExamQuestion examQuestion,
            SubmitAnswerDTO submittedAnswer) {

        if (examQuestion == null
                || examQuestion.getQuestion() == null
                || submittedAnswer == null) {
            return false;
        }

        Question question = examQuestion.getQuestion();

        List<Long> selectedAnswerIds =
                submittedAnswer.getSelectedAnswerIds();

        if (selectedAnswerIds == null) {
            selectedAnswerIds = List.of();
        }

        Set<Long> selectedIds =
                new HashSet<>(selectedAnswerIds);

        Set<Long> correctIds =
                question.getAnswers()
                        .stream()
                        .filter(Answer::isCorrect)
                        .map(Answer::getId)
                        .collect(Collectors.toSet());

        if (selectedIds.isEmpty()) {
            return false;
        }

        switch (question.getQuestionType()) {

            case SINGLE_CHOICE:
            case TRUE_FALSE:
                return selectedIds.size() == 1
                        && correctIds.size() == 1
                        && selectedIds.equals(correctIds);

            case MULTIPLE_CHOICE:
                return selectedIds.equals(correctIds);

            default:
                return false;
        }
    }

    @Override
    public double calculateScore(
            ExamQuestion examQuestion,
            SubmitAnswerDTO submittedAnswer) {

        if (!isAnswerCorrect(examQuestion, submittedAnswer)) {
            return 0.0;
        }

        Double score = examQuestion.getCustomScore();

        if (score == null) {
            score = examQuestion.getQuestion().getScore();
        }

        return score != null ? score : 0.0;
    }
}