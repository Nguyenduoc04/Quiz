package com.codegym.Quiz.dto;

public class SubmitAnswerDTO {
    private Long questionId;
    private Long selectedAnswerId;

    public SubmitAnswerDTO() {}
    public SubmitAnswerDTO(Long questionId, Long selectedAnswerId) {
        this.questionId = questionId;
        this.selectedAnswerId = selectedAnswerId;
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getSelectedAnswerId() { return selectedAnswerId; }
    public void setSelectedAnswerId(Long selectedAnswerId) { this.selectedAnswerId = selectedAnswerId; }
}
