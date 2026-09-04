package com.codegym.Quiz.service.impl;

import com.codegym.Quiz.dto.ExamGradingDetailDTO;
import com.codegym.Quiz.dto.ExamGradingResultDTO;
import com.codegym.Quiz.dto.SubmitAnswerDTO;
import com.codegym.Quiz.dto.SubmitExamDTO;
import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamQuestion;
import com.codegym.Quiz.repository.ExamQuestionRepository;
import com.codegym.Quiz.repository.ExamRepository;
import com.codegym.Quiz.service.ExamGradingService;
import com.codegym.Quiz.service.GradingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ExamGradingServiceImpl implements ExamGradingService {

    private final ExamRepository examRepository;

    private final ExamQuestionRepository examQuestionRepository;

    private final GradingService gradingService;

    public ExamGradingServiceImpl(
            ExamRepository examRepository,
            ExamQuestionRepository examQuestionRepository,
            GradingService gradingService) {

        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.gradingService = gradingService;
    }

    @Override
    public ExamGradingResultDTO gradeExam(
            SubmitExamDTO submitExamDTO) {

        if (submitExamDTO == null
                || submitExamDTO.getExamId() == null) {

            throw new IllegalArgumentException(
                    "Dữ liệu bài thi không hợp lệ"
            );
        }

        Exam exam = examRepository
                .findById(submitExamDTO.getExamId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy đề thi ID: "
                                        + submitExamDTO.getExamId()
                        )
                );

        List<ExamQuestion> examQuestions =
                examQuestionRepository
                        .findByExamIdOrderByQuestionOrderAsc(
                                exam.getId()
                        );

        Map<Long, SubmitAnswerDTO> submittedAnswers =
                buildSubmittedAnswerMap(
                        submitExamDTO.getAnswers()
                );

        double totalScore = 0.0;
        double score = 0.0;

        int correctAnswers = 0;

        ExamGradingResultDTO result =
                new ExamGradingResultDTO();

        for (ExamQuestion examQuestion : examQuestions) {

            Long questionId =
                    examQuestion.getQuestion().getId();

            SubmitAnswerDTO submittedAnswer =
                    submittedAnswers.get(questionId);

            boolean correct =
                    gradingService.isAnswerCorrect(
                            examQuestion,
                            submittedAnswer
                    );

            double maxScore =
                    getQuestionMaxScore(examQuestion);

            double awardedScore =
                    gradingService.calculateScore(
                            examQuestion,
                            submittedAnswer
                    );

            totalScore += maxScore;
            score += awardedScore;

            if (correct) {
                correctAnswers++;
            }

            ExamGradingDetailDTO detail =
                    new ExamGradingDetailDTO(
                            questionId,
                            correct,
                            awardedScore,
                            maxScore
                    );

            result.getDetails().add(detail);
        }

        result.setScore(score);
        result.setTotalScore(totalScore);
        result.setCorrectAnswers(correctAnswers);
        result.setTotalQuestions(
                examQuestions.size()
        );

        Double passingScore =
                exam.getPassingScore();

        result.setPassed(
                passingScore != null
                        && score >= passingScore
        );

        return result;
    }

    private Map<Long, SubmitAnswerDTO>
    buildSubmittedAnswerMap(
            List<SubmitAnswerDTO> answers) {

        Map<Long, SubmitAnswerDTO> map =
                new HashMap<>();

        if (answers == null) {
            return map;
        }

        for (SubmitAnswerDTO answer : answers) {

            if (answer == null
                    || answer.getQuestionId() == null) {
                continue;
            }

            map.put(
                    answer.getQuestionId(),
                    answer
            );
        }

        return map;
    }

    private double getQuestionMaxScore(
            ExamQuestion examQuestion) {

        if (examQuestion.getCustomScore() != null) {
            return examQuestion.getCustomScore();
        }

        Double score =
                examQuestion
                        .getQuestion()
                        .getScore();

        return score != null ? score : 0.0;
    }
}