package com.codegym.Quiz.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity: Câu hỏi (Question)
 * - Thuộc 1 danh mục (Category)
 * - Do 1 giáo viên (User) tạo ra
 * - Có nhiều đáp án (Answer)
 * - Có thể được gán vào nhiều bài thi (ExamQuestion)
 */
@Entity
@Table(name = "questions")
public class Question {

    /** Loại câu hỏi */
    public enum QuestionType {
        SINGLE_CHOICE,   // Một đáp án đúng
        MULTIPLE_CHOICE, // Nhiều đáp án đúng
        TRUE_FALSE       // Đúng/Sai
    }

    /** Mức độ khó */
    public enum DifficultyLevel {
        EASY,
        MEDIUM,
        HARD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nội dung câu hỏi – bắt buộc */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Loại câu hỏi */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType questionType = QuestionType.SINGLE_CHOICE;

    /** Mức độ khó */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;

    /** Điểm số cho câu hỏi này */
    @Column(nullable = false)
    private Double score = 1.0;

    /** Giải thích đáp án (tùy chọn) */
    @Column(columnDefinition = "TEXT")
    private String explanation;

    /** Danh mục câu hỏi */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** Người tạo câu hỏi */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /** Thời điểm tạo */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật lần cuối */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Danh sách đáp án của câu hỏi này */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Answer> answers = new ArrayList<>();

    // ─── Lifecycle callbacks ───────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ─── Constructors ──────────────────────────────────────────────────────

    public Question() {}

    public Question(String content) {
        this.content = content;
    }

    public Question(String content, QuestionType questionType, DifficultyLevel difficultyLevel,
                    Double score, Category category, User createdBy) {
        this.content = content;
        this.questionType = questionType;
        this.difficultyLevel = difficultyLevel;
        this.score = score;
        this.category = category;
        this.createdBy = createdBy;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Answer> getAnswers() { return answers; }
    public void setAnswers(List<Answer> answers) { this.answers = answers; }
}

