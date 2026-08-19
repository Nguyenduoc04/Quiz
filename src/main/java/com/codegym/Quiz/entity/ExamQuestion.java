package com.codegym.Quiz.entity;

import jakarta.persistence.*;

/**
 * Entity: Bảng trung gian giữa Exam và Question (ExamQuestion)
 * - Lưu thứ tự câu hỏi trong bài thi và điểm riêng (nếu override)
 */
@Entity
@Table(name = "exam_questions",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_exam_question",
               columnNames = {"exam_id", "question_id"}
       ))
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Bài thi */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    /** Câu hỏi */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /** Thứ tự câu hỏi trong bài thi */
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder = 0;

    /** Điểm riêng cho câu hỏi này trong bài thi (override điểm mặc định của Question) */
    @Column(name = "custom_score")
    private Double customScore;

    // ─── Constructors ──────────────────────────────────────────────────────

    public ExamQuestion() {}

    public ExamQuestion(Exam exam, Question question, Integer questionOrder) {
        this(exam, question, questionOrder, null);
    }

    public ExamQuestion(Exam exam, Question question, Integer questionOrder, Double customScore) {
        this.exam = exam;
        this.question = question;
        this.questionOrder = questionOrder;
        this.customScore = customScore;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public Integer getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }

    public Double getCustomScore() { return customScore; }
    public void setCustomScore(Double customScore) { this.customScore = customScore; }
}

