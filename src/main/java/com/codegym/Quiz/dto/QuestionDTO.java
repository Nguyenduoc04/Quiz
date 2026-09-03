package com.codegym.Quiz.dto;

import com.codegym.Quiz.entity.Question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuestionDTO {

    private Long id;
    private String content;

    private Question.QuestionType questionType =
            Question.QuestionType.SINGLE_CHOICE;

    private Question.DifficultyLevel difficultyLevel =
            Question.DifficultyLevel.MEDIUM;

    private Double score = 1.0;
    private String explanation;

    private Long categoryId;
    private String categoryName;

    private Long createdById;
    private String createdByName;

    private LocalDateTime createdAt;

    private List<AnswerDTO> answers = new ArrayList<>();

    public QuestionDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Question.QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(Question.QuestionType questionType) {
        this.questionType = questionType;
    }

    public Question.DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Question.DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<AnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerDTO> answers) {
        this.answers = answers;
    }
}