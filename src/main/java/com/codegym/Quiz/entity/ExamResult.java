package com.codegym.Quiz.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity: Kết quả một lần làm bài thi của học viên
 */
@Entity
@Table(name = "exam_results")
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Bài thi */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    /** Học viên làm bài */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /** Tổng điểm học viên đạt được */
    @Column(name = "score", nullable = false)
    private Double score = 0.0;

    /** Tổng điểm tối đa của bài thi */
    @Column(name = "total_score", nullable = false)
    private Double totalScore = 0.0;

    /** Số câu trả lời đúng */
    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers = 0;

    /** Tổng số câu hỏi */
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions = 0;

    /** Kết quả đạt / không đạt */
    @Column(name = "passed", nullable = false)
    private boolean passed = false;

    /** Thời điểm bắt đầu làm bài */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /** Thời điểm nộp bài */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /** Thời gian làm bài thực tế, tính bằng giây */
    @Column(name = "duration_seconds")
    private Long durationSeconds;

    /** Chi tiết từng câu trả lời */
    @OneToMany(
            mappedBy = "examResult",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ExamResultDetail> details = new ArrayList<>();

    public ExamResult() {
    }

    public ExamResult(Exam exam, User student) {
        this.exam = exam;
        this.student = student;
        this.startedAt = LocalDateTime.now();
    }

    // ─── Helper methods ─────────────────────────────────

    public void addDetail(ExamResultDetail detail) {
        details.add(detail);
        detail.setExamResult(this);
    }

    public void removeDetail(ExamResultDetail detail) {
        details.remove(detail);
        detail.setExamResult(null);
    }

    // ─── Getters & Setters ──────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public List<ExamResultDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ExamResultDetail> details) {
        this.details = details;
    }
}