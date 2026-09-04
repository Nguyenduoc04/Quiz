package com.codegym.Quiz.dto;

import java.util.ArrayList;
import java.util.List;

public class SubmitExamDTO {
    private Long examId;
    private List<SubmitAnswerDTO> answers = new ArrayList<>();

    public SubmitExamDTO() {}
    public SubmitExamDTO(Long examId, List<SubmitAnswerDTO> answers) {
        this.examId = examId;
        this.answers = answers;
    }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public List<SubmitAnswerDTO> getAnswers() { return answers; }
    public void setAnswers(List<SubmitAnswerDTO> answers) { this.answers = answers; }
}
