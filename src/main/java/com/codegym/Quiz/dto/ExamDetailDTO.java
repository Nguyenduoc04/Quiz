package com.codegym.Quiz.dto;

import com.codegym.Quiz.entity.ExamStatus;

import java.util.ArrayList;
import java.util.List;

public class ExamDetailDTO {

    private Long id;

    private String title;

    private String description;

    private Integer durationMinutes;

    private Double totalScore;

    private Double passingScore;

    private ExamStatus status;

    private Integer totalQuestions;

    private List<ExamQuestionDetailDTO> questions = new ArrayList<>();

    public ExamDetailDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Double getPassingScore() {
        return passingScore;
    }

    public void setPassingScore(Double passingScore) {
        this.passingScore = passingScore;
    }

    public ExamStatus getStatus() {
        return status;
    }

    public void setStatus(ExamStatus status) {
        this.status = status;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public List<ExamQuestionDetailDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ExamQuestionDetailDTO> questions) {
        this.questions = questions;
    }
}