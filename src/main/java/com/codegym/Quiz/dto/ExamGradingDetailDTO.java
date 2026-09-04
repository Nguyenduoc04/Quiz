package com.codegym.Quiz.dto;

/**
 * Kết quả chấm của một câu hỏi.
 */
public class ExamGradingDetailDTO {

    private Long questionId;

    private boolean correct;

    private Double scoreAwarded;

    private Double maxScore;

    public ExamGradingDetailDTO() {
    }

    public ExamGradingDetailDTO(
            Long questionId,
            boolean correct,
            Double scoreAwarded,
            Double maxScore) {

        this.questionId = questionId;
        this.correct = correct;
        this.scoreAwarded = scoreAwarded;
        this.maxScore = maxScore;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public Double getScoreAwarded() {
        return scoreAwarded;
    }

    public void setScoreAwarded(Double scoreAwarded) {
        this.scoreAwarded = scoreAwarded;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
    }
}