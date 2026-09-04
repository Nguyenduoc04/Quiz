package com.codegym.Quiz.entity;

import jakarta.persistence.*;

/**
 * Entity: Chi tiết câu trả lời của học viên
 * trong một lần làm bài thi
 */
@Entity
@Table(
        name = "exam_result_details",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_exam_result_question",
                columnNames = {"exam_result_id", "question_id"}
        )
)
public class ExamResultDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Kết quả bài thi */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_result_id", nullable = false)
    private ExamResult examResult;

    /** Câu hỏi */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * Các đáp án người dùng chọn.
     *
     * Dùng chuỗi ID để hỗ trợ cả:
     * - SINGLE_CHOICE
     * - TRUE_FALSE
     * - MULTIPLE_CHOICE
     *
     * Ví dụ:
     * "4"
     * "2,5,7"
     */
    @Column(name = "selected_answer_ids", columnDefinition = "TEXT")
    private String selectedAnswerIds;

    /** Câu này có đúng hoàn toàn không */
    @Column(name = "is_correct", nullable = false)
    private boolean correct = false;

    /** Điểm nhận được ở câu này */
    @Column(name = "score_awarded", nullable = false)
    private Double scoreAwarded = 0.0;

    /** Điểm tối đa của câu hỏi trong bài thi */
    @Column(name = "max_score", nullable = false)
    private Double maxScore = 0.0;

    public ExamResultDetail() {
    }

    public ExamResultDetail(
            ExamResult examResult,
            Question question,
            String selectedAnswerIds) {

        this.examResult = examResult;
        this.question = question;
        this.selectedAnswerIds = selectedAnswerIds;
    }

    // ─── Getters & Setters ──────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExamResult getExamResult() {
        return examResult;
    }

    public void setExamResult(ExamResult examResult) {
        this.examResult = examResult;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getSelectedAnswerIds() {
        return selectedAnswerIds;
    }

    public void setSelectedAnswerIds(String selectedAnswerIds) {
        this.selectedAnswerIds = selectedAnswerIds;
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