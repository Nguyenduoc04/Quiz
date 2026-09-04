package com.codegym.Quiz.dto;

public class QuestionReviewDTO {
    private Long questionId;
    private String questionContent;
    private Long selectedAnswerId;
    private String selectedAnswerContent;
    private String correctAnswerContent;
    private Boolean isCorrect;

    public QuestionReviewDTO() {}

    public QuestionReviewDTO(Long questionId, String questionContent, Long selectedAnswerId,
                             String selectedAnswerContent, String correctAnswerContent, Boolean isCorrect) {
        this.questionId = questionId;
        this.questionContent = questionContent;
        this.selectedAnswerId = selectedAnswerId;
        this.selectedAnswerContent = selectedAnswerContent;
        this.correctAnswerContent = correctAnswerContent;
        this.isCorrect = isCorrect;
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }

    public Long getSelectedAnswerId() { return selectedAnswerId; }
    public void setSelectedAnswerId(Long selectedAnswerId) { this.selectedAnswerId = selectedAnswerId; }

    public String getSelectedAnswerContent() { return selectedAnswerContent; }
    public void setSelectedAnswerContent(String selectedAnswerContent) { this.selectedAnswerContent = selectedAnswerContent; }

    public String getCorrectAnswerContent() { return correctAnswerContent; }
    public void setCorrectAnswerContent(String correctAnswerContent) { this.correctAnswerContent = correctAnswerContent; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
}
