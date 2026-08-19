package com.codegym.Quiz.entity;

import jakarta.persistence.*;

/**
 * Entity: Đáp án (Answer)
 * - Mỗi câu hỏi có nhiều đáp án
 * - isCorrect = true nếu đây là đáp án đúng
 */
@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nội dung đáp án – bắt buộc */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Đánh dấu đây có phải đáp án đúng không */
    @Column(name = "is_correct", nullable = false)
    private boolean correct = false;

    /** Thứ tự hiển thị (A, B, C, D...) */
    @Column(name = "display_order")
    private Integer displayOrder;

    /** Câu hỏi mà đáp án này thuộc về */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // ─── Constructors ──────────────────────────────────────────────────────

    public Answer() {}

    public Answer(String content, boolean correct, Integer displayOrder, Question question) {
        this.content = content;
        this.correct = correct;
        this.displayOrder = displayOrder;
        this.question = question;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
}
