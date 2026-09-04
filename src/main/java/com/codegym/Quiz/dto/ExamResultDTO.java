package com.codegym.Quiz.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExamResultDTO {
    private Long id;
    private Long examId;
    private String examTitle;
    private Double score;
    private Double totalScore;
    private Integer correctCount;
    private Integer totalQuestions;
    private Boolean isPassed;
    private LocalDateTime submittedAt;
    private String studentName;
    private List<QuestionReviewDTO> details = new ArrayList<>();

    public ExamResultDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Boolean getIsPassed() { return isPassed; }
    public void setIsPassed(Boolean isPassed) { this.isPassed = isPassed; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public List<QuestionReviewDTO> getDetails() { return details; }
    public void setDetails(List<QuestionReviewDTO> details) { this.details = details; }
}
