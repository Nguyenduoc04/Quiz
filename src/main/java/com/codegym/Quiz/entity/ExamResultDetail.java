package com.codegym.Quiz.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "exam_result_details")
public class ExamResultDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_result_id", nullable = false)
    private ExamResult examResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private Long selectedAnswerId;
    private Boolean isCorrect = false;
    private Double scoreObtained = 0.0;

    public ExamResultDetail() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExamResult getExamResult() { return examResult; }
    public void setExamResult(ExamResult examResult) { this.examResult = examResult; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public Long getSelectedAnswerId() { return selectedAnswerId; }
    public void setSelectedAnswerId(Long selectedAnswerId) { this.selectedAnswerId = selectedAnswerId; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Double getScoreObtained() { return scoreObtained; }
    public void setScoreObtained(Double scoreObtained) { this.scoreObtained = scoreObtained; }
}
