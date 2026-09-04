package com.codegym.Quiz.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO nội bộ dùng cho kết quả chấm toàn bộ bài thi.
 */
public class ExamGradingResultDTO {

    private Double score;
    private Double totalScore;

    private Integer correctAnswers;
    private Integer totalQuestions;

    private boolean passed;

    private List<ExamGradingDetailDTO> details = new ArrayList<>();

    public ExamGradingResultDTO() {
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public List<ExamGradingDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<ExamGradingDetailDTO> details) {
        this.details = details;
    }
}