package com.codegym.Quiz.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity: Bài thi (Exam)
 * - Do 1 Giáo viên (User) tạo ra
 * - Chứa nhiều câu hỏi thông qua ExamQuestion (bảng trung gian)
 */
@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tiêu đề bài thi – bắt buộc */
    @Column(nullable = false, length = 200)
    private String title;

    /** Mô tả bài thi */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Thời gian làm bài (phút) */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 60;

    /** Tổng điểm bài thi */
    @Column(name = "total_score")
    private Double totalScore = 10.0;

    /** Điểm đạt */
    @Column(name = "passing_score")
    private Double passingScore = 5.0;

    /** Trạng thái bài thi */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamStatus status = ExamStatus.DRAFT;

    /** Giáo viên tạo bài thi */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /** Thời điểm tạo */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật lần cuối */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Danh sách câu hỏi trong bài thi (qua bảng trung gian ExamQuestion) */
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ExamQuestion> examQuestions = new ArrayList<>();

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

    public Exam() {}

    public Exam(String title, String description, Integer durationMinutes,
                Double totalScore, Double passingScore, User createdBy) {
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.totalScore = totalScore;
        this.passingScore = passingScore;
        this.createdBy = createdBy;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }

    public Double getPassingScore() { return passingScore; }
    public void setPassingScore(Double passingScore) { this.passingScore = passingScore; }

    public ExamStatus getStatus() { return status; }
    public void setStatus(ExamStatus status) { this.status = status; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ExamQuestion> getExamQuestions() { return examQuestions; }
    public void setExamQuestions(List<ExamQuestion> examQuestions) { this.examQuestions = examQuestions; }
}

