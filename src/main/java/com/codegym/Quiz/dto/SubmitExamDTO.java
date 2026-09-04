package com.codegym.Quiz.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO: Dữ liệu học viên gửi lên khi nộp bài thi
 */
public class SubmitExamDTO {

    private Long examId;

    private LocalDateTime startedAt;

    private List<SubmitAnswerDTO> answers = new ArrayList<>();

    public SubmitExamDTO() {
    }

    public SubmitExamDTO(
            Long examId,
            LocalDateTime startedAt,
            List<SubmitAnswerDTO> answers) {

        this.examId = examId;
        this.startedAt = startedAt;
        this.answers = answers;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public List<SubmitAnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SubmitAnswerDTO> answers) {
        this.answers = answers;
    }
}