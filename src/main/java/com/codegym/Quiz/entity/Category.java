package com.codegym.Quiz.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity: Danh mục câu hỏi (Category)
 * - Mỗi danh mục do 1 User (Admin hoặc Teacher) tạo ra
 * - Một danh mục có thể chứa nhiều câu hỏi (Question)
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên danh mục – bắt buộc, tối đa 100 ký tự */
    @Column(nullable = false, length = 100)
    private String name;

    /** Mô tả ngắn về danh mục */
    @Column(length = 500)
    private String description;

    /** Người tạo danh mục (Admin hoặc Teacher) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /** Thời điểm tạo */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật lần cuối */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Danh sách câu hỏi thuộc danh mục này */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();

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

    public Category() {}

    public Category(String name, String description, User createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}
