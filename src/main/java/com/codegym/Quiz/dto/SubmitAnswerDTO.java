package com.codegym.Quiz.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO: Câu trả lời của học viên cho một câu hỏi
 */
public class SubmitAnswerDTO {

    private Long questionId;

    /**
     * Danh sách ID đáp án học viên chọn.
     *
     * SINGLE_CHOICE / TRUE_FALSE:
     * chỉ có 1 phần tử.
     *
     * MULTIPLE_CHOICE:
     * có thể có nhiều phần tử.
     */
    private List<Long> selectedAnswerIds = new ArrayList<>();

    public SubmitAnswerDTO() {
    }

    public SubmitAnswerDTO(Long questionId, List<Long> selectedAnswerIds) {
        this.questionId = questionId;
        this.selectedAnswerIds = selectedAnswerIds;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public List<Long> getSelectedAnswerIds() {
        return selectedAnswerIds;
    }

    public void setSelectedAnswerIds(List<Long> selectedAnswerIds) {
        this.selectedAnswerIds = selectedAnswerIds;
    }
}