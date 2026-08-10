package com.codegym.Quiz.dto;

import java.time.LocalDateTime;

public class CategoryDTO {

    private Long id;
    private String name;
    private String description;
    private String createdByName;
    private Long createdById;
    private LocalDateTime createdAt;
    private long questionCount;

    public CategoryDTO() {}

    public CategoryDTO(Long id, String name, String description, String createdByName, Long createdById, LocalDateTime createdAt, long questionCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdByName = createdByName;
        this.createdById = createdById;
        this.createdAt = createdAt;
        this.questionCount = questionCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public long getQuestionCount() { return questionCount; }
    public void setQuestionCount(long questionCount) { this.questionCount = questionCount; }
}
